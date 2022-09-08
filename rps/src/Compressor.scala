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