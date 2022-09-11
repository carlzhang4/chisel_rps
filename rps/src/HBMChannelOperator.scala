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
import common.storage.RegSlice
import common.axi.AXI_HBM_R

class ChannelWriter(index:Int) extends Module{
	def TODO_32 = 32
	def PACK_SIZE = 4*1024
	val io = IO(new Bundle{
		val aw			= Decoupled(AXI_HBM_ADDR())
		val w			= Decoupled(AXI_HBM_W())
		val recv_meta	= Flipped(Decoupled(new RECV_META))
		val recv_data	= Flipped(Decoupled(new AXIS(512)))
	})
	val slice_aw		= Module(new RegSlice(chiselTypeOf(io.aw.bits)))
	val slice_w			= Module(new RegSlice(chiselTypeOf(io.w.bits)))
	io.aw				<> slice_aw.io.downStream
	io.w				<> slice_w.io.downStream

	val reg_aw 			= slice_aw.io.upStream
	val reg_w 			= slice_w.io.upStream
	reg_aw.bits.hbm_init()
	reg_w.bits.hbm_init()

	val recv_meta		= XQueue(io.recv_meta, TODO_32)
	val recv_data		= XQueue(io.recv_data, PACK_SIZE/64)//hold at least a packet

	//aw
	{
		val sParseCmd :: sSendAw :: sEnd :: Nil = Enum(3)

		val reg_addr				= RegInit(UInt(28.W), 0.U)//256M
		val en 						= reg_aw.fire()
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

		reg_aw.valid					:= state === sSendAw
		reg_aw.bits.addr				:= (256*1024*1024*index).U + reg_addr
		reg_aw.bits.len				:= 0xf.U //hbm aw max 16 beats

		when(reg_aw.fire()){
			reg_addr 				:= reg_addr+fire_bytes
		}
	}

	//w
	{
		val (counter,wrap)			= Counter(reg_w.fire(), 16)//hbm aw max 16beats

		val reg_data		= RegInit(UInt(512.W),0.U)
		val sFetch :: sFirst :: sSecond :: Nil = Enum(3)
		val state = RegInit(sFetch)
		switch(state){
			is(sFetch){
				reg_data		:= recv_data.bits.data
				when(recv_data.fire()){
					when(reg_w.fire()){
						state	:= sSecond
					}.otherwise{
						state	:= sFirst
					}
				}
			}
			is(sFirst){
				when(reg_w.fire()){
					state		:= sSecond
				}
			}
			is(sSecond){
				when(reg_w.fire()){
					state		:= sFetch
				}
			}
		}
		recv_data.ready	:= state===sFetch
		when(state===sFetch){
			reg_w.valid	:= recv_data.valid
		}.otherwise{
			reg_w.valid	:= 1.U
		}
		when(state===sFetch){
			reg_w.bits.data	:= recv_data.bits.data(255,0)
		}.elsewhen(state===sFirst){
			reg_w.bits.data	:= reg_data(255,0)
		}.otherwise{
			reg_w.bits.data	:= reg_data(511,256)
		}
		reg_w.bits.last		:= wrap
	}
	
}

class ChannelReader(Factor:Int) extends Module{
	/*
	* hbm port 256 bits = 32 byte
	* ar max 16 beats = 512 bytes
	* 4K data requires 8 ar beats and 128 r beats
	* 4K data out is 512 bits width and has 64 beats
	*/
	def TODO_RAW_PACKET_SIZE 		= 4096 //byte
	def TODO_RAW_PACKET_SIZE_BEATS 	= 64 //4096/64
	def TODO_32 = 32
	val io = IO(new Bundle{
		val ar		= Decoupled(AXI_HBM_ADDR())
		val r		= Flipped(Decoupled(AXI_HBM_R()))
		val cmd_in	= Flipped(Decoupled(new Meta2Compressor))
		val out 	= Decoupled(new CompressData)
	})

	io.ar.bits.hbm_init()

	val ar_bytes	= ((io.ar.bits.len+&1.U) << 5)
	val count_ar_bytes	= RegInit(UInt(32.W), 0.U)
	val ar_addr			= RegInit(UInt(33.W), 0.U)
	when(io.cmd_in.fire()){
		ar_addr		:= io.cmd_in.bits.addr
	}.elsewhen(io.ar.fire()){
		ar_addr		:= ar_addr + ar_bytes
	}

	val sParseCmd :: sSendAr :: sWait :: Nil	= Enum(3)
	val state_ar = RegInit(sParseCmd)
	switch(state_ar){
		is(sParseCmd){
			when(io.cmd_in.fire()){
				state_ar	:= sSendAr
			}
		}
		is(sSendAr){
			when(io.ar.fire() && count_ar_bytes+ar_bytes === TODO_RAW_PACKET_SIZE.U){
				state_ar	:= sParseCmd
			}
		}
	}
	io.cmd_in.ready := state_ar === sParseCmd

	io.ar.valid		:= state_ar === sSendAr
	io.ar.bits.addr	:= ar_addr
	io.ar.bits.len	:= 0xf.U

	when(io.ar.fire()){
		count_ar_bytes 		:= count_ar_bytes + ar_bytes
		when(count_ar_bytes+ar_bytes === TODO_RAW_PACKET_SIZE.U){
			count_ar_bytes	:= 0.U
		}
	}

	val r_delay		= RegSlice(io.r)

	//ar and data
	val q_data		= XQueue(new CompressData, TODO_32)

	val first_data = RegInit(UInt(256.W), 0.U)
	val sFirst :: sSecond :: Nil = Enum(2)
	val state_r = RegInit(sFirst)
	switch(state_r){
		is(sFirst){
			when(r_delay.fire()){
				state_r		:= sSecond
				first_data	:= r_delay.bits.data
			}
		}
		is(sSecond){
			when(r_delay.fire()){
				state_r		:= sFirst
			}
		}
	}
	when(state_r === sFirst){
		r_delay.ready	:= 1.U
	}.elsewhen(state_r === sSecond){
		r_delay.ready	:= q_data.io.in.ready
	}.otherwise{
		r_delay.ready	:= 0.U //undefined state
	}

	val en_push						= q_data.io.in.fire()
	val (count_push,wrap_push)		= Counter(en_push, TODO_RAW_PACKET_SIZE_BEATS)
	// val count_push 	= RegInit(UInt(32.W),0.U)
	
	q_data.io.in.valid		:= state_r === sSecond && r_delay.valid
	q_data.io.in.bits.data	:= Cat(r_delay.bits.data, first_data)
	q_data.io.in.bits.last	:= wrap_push//count_push+64.U === TODO_RAW_PACKET_SIZE.U
	
	val compressBlock		= Module(new CompressBlock(Factor))
	compressBlock.io.in	<> q_data.io.out
	io.out	<> compressBlock.io.out
}