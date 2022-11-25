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
import network.roce.util.TX_META
import network.roce.util.RECV_META
import network.roce.util.APP_OP_CODE
import common.Collector
import common.Timer
import common.ToZero
import common.storage.RegSlice
import chisel3.util.experimental.BoringUtils
import common.OffsetGenerator
import common.connection.CreditQ
import common.BaseILA
import common.Statistics
import common.connection.ProducerConsumer
import common.connection.SimpleRouter
import common.connection.XArbiter


class ClientReqInterface(NumChannels:Int,Factor:Int) extends Module{
	def CLIENT_HOST_MEM_OFFSET = 0
	def HOST_MEM_PARTITION = 1024L*1024*1024

	val io = IO(new Bundle{
		val recv_meta		= Flipped(Decoupled(new RECV_META))
		val recv_data		= Flipped(Decoupled(AXIS(512)))
		val axi_hbm			= Vec(NumChannels,AXI_HBM())

		val writeCMD		= Decoupled(new WriteCMD)
		val writeData		= Decoupled(new WriteData)
		val axib_data		= Flipped(Decoupled(new AxiBridgeData))

		val send_meta		= Decoupled(new TX_META)
		val send_data		= Decoupled(AXIS(512))

		//h2c
		val readCMD			= Decoupled(new ReadCMD)
		val readData		= Flipped(Decoupled(new ReadData))
	})
}

class ClientReqHandler(NumChannels:Int=4, Factor:Int=12, Index:Int=0) extends ClientReqInterface(NumChannels,Factor){
	def TODO_32 = 32
	def PACK_SIZE = 4*1024

	io.readCMD.valid		:= 0.U
	ToZero(io.readCMD.bits)
	io.readData.ready		:= 1.U

	val client_req_qdma_latency = Timer(io.writeCMD.fire(), io.axib_data.fire()).latency

	Collector.report(client_req_qdma_latency,fix_str = "REG_CLIENT_REQ_QDMA_LATENCY")

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
	val q_2host_meta						= XQueue(new WriteCMD, TODO_32)
	val q_2host_data						= XQueue(new WriteData, TODO_32)
	val reg_offset							= RegInit(UInt(48.W),0.U)

	val read_hbm_addr	= OffsetGenerator(
		num 	= NumChannels.U,
		range 	= (256*1024*1024).U,
		step 	= PACK_SIZE.U,
		en 		= q_compress_cmd.io.in.fire(),
		addr_width = 33
	)
	
	Connection.one2many(q_meta_dup.io.out)(q_2host_meta.io.in, q_2host_data.io.in, q_compress_cmd.io.in)
	q_2host_meta.io.in.bits.addr_offset		:= (reg_offset%HOST_MEM_PARTITION.U)+CLIENT_HOST_MEM_OFFSET.U
	q_2host_meta.io.in.bits.len				:= 64.U
	q_2host_data.io.in.bits.data			:= reg_offset+CLIENT_HOST_MEM_OFFSET.U+1.U
	q_2host_data.io.in.bits.last			:= 1.U

	q_compress_cmd.io.in.bits.addr			:= read_hbm_addr + (1L*GlobalConfig.ChannelOffset(Index)*256*1024*1024).U

	q_2host_meta.io.out						<> io.writeCMD
	q_2host_data.io.out						<> io.writeData

	when(q_2host_meta.io.in.fire()){//when meta fires, data fires too
		reg_offset							:= reg_offset+64.U
	}

	//compress cmd directly, can add a many2one to be controlled by cpu
	compressorHBM.io.cmd_in				<> q_compress_cmd.io.out
	Connection.one2one(io.axib_data)(io.send_meta)
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
				val t = Module(new ChannelWriter(i+GlobalConfig.ChannelOffset(Index)))
				t.io.aw	<> io.axi_hbm(i).aw
				t.io.w	<> io.axi_hbm(i).w
				t.io.recv_meta	<> router.io.out_meta(i)
				t.io.recv_data	<> router.io.out_data(i)
				t
			}	
		}
	}

	Collector.fire(io.recv_meta)
	Collector.fire(io.recv_data)
	Collector.fireLast(io.recv_data)
	Collector.fire(io.writeCMD)
	Collector.fire(io.writeData)
	Collector.fireLast(io.writeData)
	Collector.fire(io.axib_data)
	Collector.fire(io.send_meta)
	Collector.fire(io.send_data)
	Collector.fireLast(io.send_data)
}

class WorkQueue(val credits:Int, val range:Long, val index:Int) extends Module{
	def PacketSize = 4096
	def TODO_32 = 32
	def CompressedSize = 2048
	def CLIENT_HOST_MEM_OFFSET = 0
	val io = IO(new Bundle{
		val recv_meta		= Flipped(Decoupled(new RECV_META))
		val axib_data		= Flipped(Decoupled(new AxiBridgeData))
		val writeCMD		= Decoupled(new WriteCMD)
		val readCMD			= Decoupled(new ReadCMD)
	})

	val creditQ				= Module(new CreditQ())//must minus max threads num
	creditQ.io.maxCredit	:= credits.U
	val offset	= OffsetGenerator(
		num 	= 1.U,
		range 	= range.U,
		step 	= PacketSize.U,
		en 		= io.writeCMD.fire()
	)


	val recv_meta					= XQueue(io.recv_meta, TODO_32)
	Connection.one2many(recv_meta)(io.writeCMD,creditQ.io.in)
	io.writeCMD.bits.addr_offset	:= offset + CLIENT_HOST_MEM_OFFSET.U + (range*index).U
	io.writeCMD.bits.len			:= PacketSize.U


	Connection.many2one(io.axib_data,creditQ.io.out)(io.readCMD)

	io.readCMD.bits.addr_offset		:= io.axib_data.bits.data(32,0)
	io.readCMD.bits.len				:= CompressedSize.U
}

class DummyClientReqHandler extends ClientReqInterface(0,0){
	def TODO_32 = 32
	def TODO_1024 = 1024
	def PacketSize = 4096
	def CompressedSize = 2048
	def NumThreads = 32
	/*
		recv_meta -> writeCMD -> axib_data -> readCMD -> readData -> send_meta
	*/

	val recv_meta					= RegSlice(io.recv_meta)
	val axib_data					= XQueue(io.axib_data, TODO_1024)

	val workQueues	= {
		for(i<-0 until NumThreads)yield{
			val credits	= (HOST_MEM_PARTITION/PacketSize/NumThreads).toInt
			val range 	= HOST_MEM_PARTITION/NumThreads
			val tmp = Module(new WorkQueue(credits,range,i))
			tmp
		}	
	}
	val pc							= ProducerConsumer(Seq(4,8))(recv_meta,workQueues.map(_.io.recv_meta))
	val arbiterWriteCMD				= XArbiter(Seq(4,8))(workQueues.map(_.io.writeCMD),io.writeCMD)
	val arbiterReadCMD				= XArbiter(Seq(4,8))(workQueues.map(_.io.readCMD),io.readCMD)

	val routerFirstLevel			= SimpleRouter(new AxiBridgeData, 4)
	val routerSecondLevel			= Seq.fill(4)(SimpleRouter(new AxiBridgeData,8))
	routerFirstLevel.io.in			<> axib_data
	routerFirstLevel.io.idx			<> axib_data.bits.data(479,448)
	for(i<-0 until 4){
		routerFirstLevel.io.out(i)	<> routerSecondLevel(i).io.in //workQueues(i).io.axib_data
		routerSecondLevel(i).io.idx	<> routerSecondLevel(i).io.in.bits.data(479,448+2)
		for(j<-0 until 8){
			routerSecondLevel(i).io.out(j)	<> workQueues(i*8+j).io.axib_data
		}
	}
	

	val recv_data			= XQueue(io.recv_data, TODO_32)
	Connection.one2one(recv_data)(io.writeData)
	io.writeData.bits.data	<> recv_data.bits.data
	io.writeData.bits.last	<> recv_data.bits.last

	// class ila_bench(seq:Seq[Data]) extends BaseILA(seq)	  
  	// val inst_bench = Module(new ila_bench(Seq(	
	// 	io.writeCMD,
	// 	io.readCMD,
	// 	io.axib_data,
  	// )))
  	// inst_bench.connect(clock)
	

	val read_data_stall				= Statistics.count(io.readData.valid & !io.readData.ready)
	Collector.report(read_data_stall)


	val readDataDelay				= RegSlice(io.readData)
	val q_send_data					= XQueue(AXIS(512),TODO_1024)
	Connection.one2one(readDataDelay)(q_send_data.io.in)
	ToAllOnes(q_send_data.io.in.bits.keep)
	q_send_data.io.in.bits.data		<> readDataDelay.bits.data
	q_send_data.io.in.bits.last		<> readDataDelay.bits.last
	io.send_data					<> RegSlice(q_send_data.io.out)

	val q_send_meta					= XQueue(new TX_META,TODO_32)
	val first_beat_flag				= RegInit(Bool(),true.B)
	when(q_send_data.io.in.fire() && first_beat_flag){
		q_send_meta.io.in.valid		:= 1.U
	}.otherwise{
		q_send_meta.io.in.valid		:= 0.U
	}
	q_send_meta.io.in.bits.rdma_cmd		:= APP_OP_CODE.APP_SEND
	q_send_meta.io.in.bits.qpn			:= 2.U
	q_send_meta.io.in.bits.length		:= CompressedSize.U
	q_send_meta.io.in.bits.local_vaddr	:= 0.U
	q_send_meta.io.in.bits.remote_vaddr	:= 0.U
	q_send_meta.io.out					<> io.send_meta
	when(q_send_data.io.in.fire()){
		first_beat_flag				:= false.B
		when(q_send_data.io.in.bits.last===1.U){
			first_beat_flag			:= true.B
		}
	}

	val q_send_meta_overflow = q_send_meta.io.in.valid & !q_send_meta.io.in.ready
	Collector.trigger(q_send_meta_overflow)
	
	Collector.fire(io.recv_meta)
	Collector.fire(io.recv_data)

	Collector.fire(io.writeCMD)
	Collector.fire(io.writeData)
	Collector.fireLast(io.writeData)

	Collector.fire(io.axib_data)

	Collector.fire(io.send_meta)
	Collector.fire(io.send_data)

	Collector.fire(io.readCMD)
	Collector.fire(io.readData)

	Collector.report(io.writeData.valid)
	Collector.report(io.writeData.ready)
	Collector.report(io.writeCMD.valid)
	Collector.report(io.writeCMD.ready)
	Collector.report(io.recv_meta.valid)
	Collector.report(io.recv_meta.ready)
	Collector.report(io.recv_data.valid)
	Collector.report(io.recv_data.ready)
}