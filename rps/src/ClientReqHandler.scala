package rps

import chisel3._
import chisel3.util._
import common.axi.AXIS
import common.axi.AXI_HBM
import common.axi.AXI_HBM_ADDR
import common.axi.AXI_HBM_W
import common.storage.XQueue
import common.connection.CompositeRouter
import common.ToAllOnes
import common.connection.Connection

class ChannelWriter(index:Int) extends Module{
	def TODO_32 = 32
	def PACK_SIZE = 4*1024
	val io = IO(new Bundle{
		val aw			= Decoupled(AXI_HBM_ADDR())
		val w			= Decoupled(AXI_HBM_W())
		val recv_meta	= Flipped(Decoupled(new RECV_META))
		val recv_data	= Flipped(Decoupled(new AXIS(512)))
	})
	io.aw.bits.hbm_init()
	io.w.bits.hbm_init()

	val recv_meta		= XQueue(io.recv_meta, TODO_32)
	val recv_data		= XQueue(io.recv_data, PACK_SIZE/64)//hold at least a packet

	//aw
	{
		val sParseCmd :: sSendAw :: sEnd :: Nil = Enum(3)

		val reg_addr				= RegInit(UInt(28.W), 0.U)//256M
		val en 						= io.aw.fire()
		val (counter,wrap)			= Counter((0 until PACK_SIZE by 512), en)//hbm aw max 512bytes
		val fire_bytes 				= (16.U)<<5 //hbm aw max 16 beats
		val state = RegInit(sParseCmd)
		switch(state){
			is(sParseCmd){
				when(recv_meta.fire()){
					state			:= sSendAw
				}
			}
			is(sSendAw){
				when(wrap){
					state			:= sParseCmd
				}
			}
		}
		recv_meta.ready				:= state === sParseCmd

		io.aw.valid					:= state === sSendAw
		io.aw.bits.addr				:= (256*1024*1024*index).U + reg_addr
		io.aw.bits.len				:= 0xf.U //hbm aw max 16 beats

		when(io.aw.fire()){
			reg_addr 				:= reg_addr+fire_bytes
		}
	}

	//w
	{
		val (counter,wrap)			= Counter(io.w.fire(), 16)//hbm aw max 16beats

		val reg_data		= RegInit(UInt(512.W),0.U)
		val sFetch :: sFirst :: sSecond :: Nil = Enum(3)
		val state = RegInit(sFetch)
		switch(state){
			is(sFetch){
				reg_data		:= recv_data.bits.data
				when(recv_data.fire()){
					when(io.w.fire()){
						state	:= sSecond
					}.otherwise{
						state	:= sFirst
					}
				}
			}
			is(sFirst){
				when(io.w.fire()){
					state		:= sSecond
				}
			}
			is(sSecond){
				when(io.w.fire()){
					state		:= sFetch
				}
			}
		}
		recv_data.ready	:= state===sFetch
		when(state===sFetch){
			io.w.valid	:= recv_data.valid
		}.otherwise{
			io.w.valid	:= 1.U
		}
		when(state===sFetch){
			io.w.bits.data	:= recv_data.bits.data(255,0)
		}.elsewhen(state===sFirst){
			io.w.bits.data	:= reg_data(255,0)
		}.otherwise{
			io.w.bits.data	:= reg_data(511,256)
		}
		io.w.bits.last		:= wrap
	}
	
}

class ClientReqHandler(NumChannels:Int=4, Factor:Int=12) extends Module{
	def TODO_32 = 32
	def PACK_SIZE = 4*1024
	def CLIENT_HOST_MEM_OFFSET = 0
	def HOST_MEM_PARTITION = 1024L*1024*1024
	val io = IO(new Bundle{
		val recv_meta		= Flipped(Decoupled(new RECV_META))
		val recv_data		= Flipped(Decoupled(AXIS(512)))
		val axi_hbm			= Vec(NumChannels,AXI_HBM())

		val meta2host		= Decoupled(new Meta2Host)
		val data2host		= Decoupled(new Data2Host)
		val meta_from_host	= Flipped(Decoupled(new MetaFromHost))

		val send_meta		= Decoupled(new TX_META)
		val send_data		= Decoupled(AXIS(512))
	})

	for(i<-0 until NumChannels){
		io.axi_hbm(i).hbm_init()
	}

	val q_meta				= XQueue(new RECV_META, TODO_32)
	val q_meta_dup			= XQueue(new RECV_META, TODO_32)
	q_meta.io.in.bits		:= io.recv_meta.bits
	q_meta_dup.io.in.bits	:= io.recv_meta.bits
	Connection.one2many(io.recv_meta)(q_meta.io.in,q_meta_dup.io.in)

	//read from HBM and connect to io.send_data
	val compressorHBM 	= {
		val t = Module(new CompressorHBM(NumChannels,Factor))
		for(i<-0 until NumChannels){
			t.io.hbm_ar(i)	<> io.axi_hbm(i).ar
			t.io.hbm_r(i)	<> io.axi_hbm(i).r
		}
		io.send_data.valid		:= t.io.out.valid
		io.send_data.ready		<> t.io.out.ready
		io.send_data.bits.data	:= t.io.out.bits.data
		io.send_data.bits.last	:= t.io.out.bits.last
		ToAllOnes(io.send_data.bits.keep)
		t
	}

	//use q_meta_dup to produce compress cmd
	val q_compress_cmd						= XQueue(new Meta2Compressor, TODO_32)
	val q_2host_meta						= XQueue(new Meta2Host, TODO_32)
	val q_2host_data						= XQueue(new Data2Host, TODO_32)
	val reg_addr							= RegInit(UInt(33.W),0.U)//hbm address
	val reg_offset							= RegInit(UInt(48.W),0.U)
	
	Connection.one2many(q_meta_dup.io.out)(q_2host_meta.io.in, q_2host_data.io.in, q_compress_cmd.io.in)
	q_2host_meta.io.in.bits.addr_offset		:= (reg_offset%HOST_MEM_PARTITION.U)+CLIENT_HOST_MEM_OFFSET.U
	q_2host_meta.io.in.bits.len				:= 64.U
	q_2host_data.io.in.bits.data			:= reg_offset+CLIENT_HOST_MEM_OFFSET.U+1.U
	q_2host_data.io.in.bits.last			:= 1.U
	q_compress_cmd.io.in.bits.addr			:= reg_addr

	q_2host_meta.io.out						<> io.meta2host
	q_2host_data.io.out						<> io.data2host

	when(q_2host_meta.io.in.fire()){//when meta fires, data fires too
		reg_offset							:= reg_offset+64.U
	}
	
	val en									= q_compress_cmd.io.in.fire()
	val (counter,wrap)						= Counter(en, 256*1024*1024/PACK_SIZE*NumChannels)
	when(en){
		when((counter+1.U)%NumChannels.U === 0.U){
			reg_addr					:= reg_addr + PACK_SIZE.U - (256L*1024*1024*(NumChannels-1)).U
		}.otherwise{
			reg_addr					:= reg_addr + (256L*1024*1024).U
		}
		when(wrap){
			reg_addr					:= 0.U
		}
	}

	//compress cmd directly, can add a many2one to be controlled by cpu
	compressorHBM.io.cmd_in				<> q_compress_cmd.io.out
	Connection.one2one(io.meta_from_host)(io.send_meta)
	io.send_meta.bits.rdma_cmd			:= APP_OP_CODE.APP_SEND
	io.send_meta.bits.qpn				:= 2.U
	io.send_meta.bits.length				:= (2*1024).U//todo, to ssd pack size 
	io.send_meta.bits.local_vaddr		:= 0.U
	io.send_meta.bits.remote_vaddr		:= 0.U

	//write data into HBM using recv meta/data
	{
		val router				= CompositeRouter(new RECV_META, AXIS(512), NumChannels)
		val reg_index			= RegInit(UInt(log2Up(NumChannels).W), 0.U)
		router.io.idx			:= reg_index
		router.io.in_meta		<> q_meta.io.out
		router.io.in_data		<> io.recv_data
		when(router.io.in_meta.fire()){
			reg_index			:= reg_index+1.U
			when(reg_index+1.U === NumChannels.U){
				reg_index		:= 0.U
			}
		}
		val writers	= {
			for(i<-0 until NumChannels)yield{
				val t = Module(new ChannelWriter(i))
				t.io.aw	<> io.axi_hbm(i).aw
				t.io.w	<> io.axi_hbm(i).w
				t.io.recv_meta	<> router.io.out_meta(i)
				t.io.recv_data	<> router.io.out_data(i)
				t
			}	
		}
	}

	RPSConter.record(io.recv_meta.fire(), "ClientReqHandler_RecvMetaFire")
	RPSConter.record(io.recv_data.fire(), "ClientReqHandler_RecvDataFire")
	RPSConter.record(io.recv_data.fire()&io.recv_data.bits.last.asBool(), "ClientReqHandler_RecvDataLast")
	RPSConter.record(io.meta2host.fire(), "ClientReqHandler_Meta2HostFire")
	RPSConter.record(io.data2host.fire(), "ClientReqHandler_Data2HostFire")
	RPSConter.record(io.data2host.fire()&io.data2host.bits.last.asBool(), "ClientReqHandler_Data2HostLast")
	RPSConter.record(io.meta_from_host.fire(), "ClientReqHandler_MetaFromHostFire")
	RPSConter.record(io.send_meta.fire(), "ClientReqHandler_SendMetaFire")
	RPSConter.record(io.send_data.fire(), "ClientReqHandler_SendDataFire")
	RPSConter.record(io.send_data.fire()&io.send_data.bits.last.asBool(), "ClientReqHandler_SendDataLast")
}