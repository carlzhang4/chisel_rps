package rps

import chisel3._
import chisel3.util._
import common._
import common.storage.XQueue
import common.axi._
import common.connection.SimpleRouter
import common.connection.SerialRouter
import common.connection.SerialArbiter
import common.connection.XArbiter

class CompressorHBM(val NumChannels:Int=4, Factor:Int=4) extends Module{
	def TODO_32 = 32
	val io = IO(new Bundle{
		val hbm_ar	= Vec(NumChannels, Decoupled(AXI_HBM_ADDR()))
		val hbm_r	= Vec(NumChannels, Flipped(Decoupled(AXI_HBM_R()))) 
		val cmd_in	= Flipped(Decoupled(new Meta2Compressor))
		val out 	= Decoupled(new CompressData)
	})
	val readers 	= Seq.fill(NumChannels)(Module(new ChannelReader(Factor)))

	val router		= {
		val router = SimpleRouter(new Meta2Compressor, NumChannels)
		router.io.in <> io.cmd_in
		for(i <- 0 until NumChannels){
			router.io.out(i) <> readers(i).io.cmd_in
		}
		router.io.idx	:= io.cmd_in.bits.addr(32,28) //todo 256M
		router
	}

	for(i<-0 until NumChannels){
		readers(i).io.ar	<> io.hbm_ar(i)
		readers(i).io.r		<> io.hbm_r(i)
	}

	val arbiter		= {
		val arbiter = SerialArbiter(new CompressData, NumChannels)
		for(i<-0 until NumChannels){
			arbiter.io.in(i)	<> readers(i).io.out
		}
		arbiter
	}
	arbiter.io.out	<> io.out
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

	//ar and data
	val q_data		= XQueue(new CompressData, TODO_32)

	val first_data = RegInit(UInt(256.W), 0.U)
	val sFirst :: sSecond :: Nil = Enum(2)
	val state_r = RegInit(sFirst)
	switch(state_r){
		is(sFirst){
			when(io.r.fire()){
				state_r		:= sSecond
				first_data	:= io.r.bits.data
			}
		}
		is(sSecond){
			when(io.r.fire()){
				state_r		:= sFirst
			}
		}
	}
	when(state_r === sFirst){
		io.r.ready	:= 1.U
	}.elsewhen(state_r === sSecond){
		io.r.ready	:= q_data.io.in.ready
	}.otherwise{
		io.r.ready	:= 0.U //undefined state
	}

	val en_push						= q_data.io.in.fire()
	val (count_push,wrap_push)		= Counter(en_push, TODO_RAW_PACKET_SIZE_BEATS)
	// val count_push 	= RegInit(UInt(32.W),0.U)
	
	q_data.io.in.valid		:= state_r === sSecond && io.r.valid
	q_data.io.in.bits.data	:= Cat(io.r.bits.data, first_data)
	q_data.io.in.bits.last	:= wrap_push//count_push+64.U === TODO_RAW_PACKET_SIZE.U
	
	val compressBlock		= Module(new CompressBlock(Factor))
	compressBlock.io.in	<> q_data.io.out
	io.out	<> compressBlock.io.out
}

class CompressBlock(Factor:Int) extends Module{
	val io = IO(new Bundle{
		val in	= Flipped(Decoupled(new CompressData))
		val out = Decoupled(new CompressData)
	})

	val compressUnits	= Seq.fill(Factor)(Module(new CompressUnit()))
	
	val idx	= RegInit(UInt((log2Up(Factor)).W), 0.U)
	when(io.in.fire() && io.in.bits.last===1.U){
		idx			:= idx + 1.U
		when(idx+1.U === Factor.U){
			idx		:= 0.U
		}
	}

	val router	= SerialRouter(new CompressData, Factor)
	val arbiter =  SerialArbiter(new CompressData, Factor)
	router.io.in	<> io.in
	router.io.idx	<> idx
	arbiter.io.out	<> io.out
	for(i<-0 until Factor){
		router.io.out(i)	<> compressUnits(i).io.in
		arbiter.io.in(i)	<> compressUnits(i).io.out
	}
}
class CompressUnit(CompressCycles:Int = 2200, OutBeats:Int = 32) extends Module {
	/*
		OutBeats (default 32): beats after compression, 32*512/8 = 2K
	*/
	val io = IO(new Bundle{
		val in 					= Flipped(Decoupled(new CompressData))
		val out					= Decoupled(new CompressData)
	})

	val en_beats						= Wire(Bool())
	val (count_beats,wrap_beats)		= Counter(en_beats, OutBeats)

	val en_compress						= Wire(Bool())
	val (count_compress,wrap_compress)	= Counter(en_compress, CompressCycles)

	val is_head 						= RegInit(UInt(1.W),1.U)
	val first_data						= RegInit(UInt(32.W),0.U)

	val sRecv :: sCompress :: sSend :: Nil = Enum(3)
	val state = RegInit(sRecv)
	switch(state){
		is(sRecv){
			when(io.in.fire() && io.in.bits.last===1.U){
				state := sCompress
			}
		}
		is(sCompress){
			when(wrap_compress){
				state := sSend
			}
		}
		is(sSend){
			when(wrap_beats){
				state := sRecv
			}
		}
	}
	io.in.ready		:= state === sRecv
	io.out.valid	:= state === sSend

	when(io.in.fire() && is_head===1.U){
		first_data		:= io.in.bits.data(31,0)
		is_head			:= 0.U
	}.elsewhen(state===sSend){
		is_head			:= 1.U
	}

	when(state === sCompress){
		en_compress	:= true.B
	}.otherwise{
		en_compress	:= false.B
	}

	{//out control
		en_beats		:= io.out.fire()

		when(wrap_beats){
			io.out.bits.last	:= 1.U
		}.otherwise{
			io.out.bits.last	:= 0.U
		}
		io.out.bits.data		:= first_data
	}
	

}