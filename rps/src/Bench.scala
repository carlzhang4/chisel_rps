package rps

import chisel3._
import chisel3.util._
import common.axi.AXI_HBM
import common.ToAllOnes
import common.BaseVIO
import common.BaseILA

class HBMCompress() extends Module{
	def NUM_TOTAL_CHANNELS = 32
	def init_size_byte = 32L*1024//(32L*256*1024*1024)
	val io = IO(new Bundle{
		val axi 			= Vec(NUM_TOTAL_CHANNELS,AXI_HBM())
		// val start 			= Input(UInt(1.W))
		// val start_compress	= Input(UInt(1.W))
	})

	for(i<-0 until NUM_TOTAL_CHANNELS){
		io.axi(i).hbm_init()
	}

	// val compressorHBM	= Module(new CompressorHBM)

	val start = RegInit(UInt(1.W),0.U)//io.start
	val risingStart = start===1.U & RegNext(!start)

	//init HBM
	val aw 				= io.axi(0).aw
	val w 				= io.axi(0).w
	val r_aw_addr 		= RegInit(UInt(33.W),0.U)
	val aw_bytes		= ((aw.bits.len+&1.U)<<5)
	aw.bits.addr		:= r_aw_addr

	val r_w_data		= RegInit(UInt(32.W),0.U)
	val r_w_parity		= RegInit(UInt(1.W),0.U)
	val r_w_count_data	= RegInit(UInt(33.W),0.U)
	when(r_w_parity===0.U){
		w.bits.data		:= r_w_data
	}.otherwise{
		w.bits.data		:= 0.U
	}

	ToAllOnes(aw.bits.len)
	val sIdle :: sWrite :: sEnd :: Nil = Enum(3)
	val state_aw	= RegInit(sIdle)
	val state_w		= RegInit(sIdle)

	switch(state_aw){
		is(sIdle){
			when(start===1.U){
				state_aw	:= sWrite
			}
			r_aw_addr			:= 0.U
		}
		is(sWrite){
			when(aw.fire() && r_aw_addr+aw_bytes === init_size_byte.U){
				state_aw	:= sEnd
			}
		}
		is(sEnd){
			when(risingStart === 1.U){
				state_aw	:= sIdle
			}
		}
	}

	aw.valid	:= state_aw === sWrite
	when(aw.fire()){
		r_aw_addr	:= r_aw_addr + aw_bytes
	}

	switch(state_w){
		is(sIdle){
			when(start===1.U){
				state_w		:= sWrite
			}
			r_w_count_data	:= 0.U
			r_w_data		:= 0.U
			r_w_parity		:= 0.U
		}
		is(sWrite){
			when(w.fire() && r_w_count_data+32.U === init_size_byte.U){
				state_w		:= sEnd
			}
		}
		is(sEnd){
			when(risingStart === 1.U){
				state_w	:= sIdle
			}
		}
	}
	w.valid		:= state_w === sWrite
	w.bits.last	:= (r_w_count_data+32.U)%(aw_bytes)===0.U
	when(w.fire()){
		r_w_count_data	:= r_w_count_data + 32.U
		r_w_parity		:= r_w_parity + 1.U
		when(r_w_parity === 1.U){
			r_w_data	:= r_w_data + 1.U
		}
	}
	
	val initDone = state_w===sEnd & state_aw===sEnd
	dontTouch(initDone)

	//compress start
	def TOTAL_CMDS = 256*1024
	val compressorHBM = Module(new CompressorHBM())

	for(i<-0 until compressorHBM.NUM_CHANNELS){
		compressorHBM.io.hbm_ar(i)	<> io.axi(i).ar
		compressorHBM.io.hbm_r(i)	<> io.axi(i).r
	}

	val cmd_in			= compressorHBM.io.cmd_in
	val startCompress 	= RegInit(UInt(1.W),0.U)//io.start_compress
	val risingStartCMD	= startCompress===1.U & RegNext(!startCompress)
	val r_cmd_count		= RegInit(UInt(32.W),0.U)
	val r_cmd_addr		= RegInit(UInt(33.W),0.U)
	val state_cmd 		= RegInit(sIdle)
	switch(state_cmd){
		is(sIdle){
			when(startCompress === 1.U){
				state_cmd	:= sWrite
			}
			r_cmd_count		:= 0.U
			r_cmd_addr		:= 0.U
		}
		is(sWrite){
			when(cmd_in.fire() && r_cmd_count+1.U===TOTAL_CMDS.U){
				state_cmd	:= sEnd
			}
		}
		is(sEnd){
			when(risingStartCMD){
				state_cmd	:= sIdle
			}
		}
	}
	cmd_in.valid 		:= state_cmd===sWrite
	cmd_in.bits.addr 	:= r_cmd_addr
	when(cmd_in.fire()){
		r_cmd_count	:= r_cmd_count+1.U

		when((r_cmd_count+1.U)%compressorHBM.NUM_CHANNELS.U === 0.U){
			r_cmd_addr	:= r_cmd_addr + 4096.U - (256L*1024*1024*(compressorHBM.NUM_CHANNELS-1)).U
		}.otherwise{
			r_cmd_addr	:= r_cmd_addr + (256L*1024*1024).U
		}
	}

	val state_out			= RegInit(sIdle)
	val out					= compressorHBM.io.out
	val r_out_count 		= RegInit(UInt(32.W),0.U)
	val r_out_count_last	= RegInit(UInt(32.W),0.U)
	switch(state_out){
		is(sIdle){
			when(startCompress===1.U){
				state_out		:= sWrite
			}
			r_out_count			:= 0.U
			r_out_count_last	:= 0.U
		}
		is(sWrite){
			when(out.fire() && out.bits.last===1.U && r_out_count_last+1.U===TOTAL_CMDS.U){
				state_out		:= sEnd
			}
		}
		is(sEnd){
			when(risingStartCMD){
				state_out		:= sIdle
			}
		}
	}
	out.ready		:= state_out === sWrite
	when(out.fire()){
		r_out_count	:= r_out_count+1.U
		when(out.bits.last===1.U){
			r_out_count_last	:= r_out_count_last+1.U
		}
	}
	
	val r_count_time 	= RegInit(UInt(32.W),0.U)
	when(startCompress === 1.U){
		when(r_out_count_last === TOTAL_CMDS.U){
			r_count_time	:= r_count_time
		}.otherwise{
			r_count_time	:= r_count_time + 1.U
		}
	}.otherwise{
		r_count_time		:= 0.U
	}
	dontTouch(r_count_time)

	class vio_compress(seq:Seq[Data]) extends BaseVIO(seq)
	val mod_vio_compress = Module(new vio_compress(Seq(
		start,
		startCompress
	)))
	mod_vio_compress.connect(clock)

	val out_data = out.bits.data(63,0)

	class ila_compress(seq:Seq[Data]) extends BaseILA(seq)
	val inst = Module(new ila_compress(Seq(	
		r_aw_addr,
		r_w_data,
		r_w_count_data,
		r_cmd_count,
		r_cmd_addr,
		r_out_count,
		r_out_count_last,
		r_count_time,
		out_data,
		state_cmd,
		cmd_in.ready,
		cmd_in.valid,
	)))
	inst.connect(clock)

	def record_signals(is_high:Bool)={
		val count = RegInit(UInt(32.W),0.U)
		when(is_high){
			count	:= count+1.U
		}
		count
	}

	val fire_ar_0 	= record_signals(io.axi(0).ar.fire())
	val fire_ar_1 	= record_signals(io.axi(1).ar.fire())
	val fire_ar_2 	= record_signals(io.axi(2).ar.fire())
	val fire_ar_3 	= record_signals(io.axi(3).ar.fire())

	val fire_r_0	= record_signals(io.axi(0).r.fire())
	val fire_r_1	= record_signals(io.axi(1).r.fire())
	val fire_r_2	= record_signals(io.axi(2).r.fire())
	val fire_r_3	= record_signals(io.axi(3).r.fire())

	class ila_hbm(seq:Seq[Data]) extends BaseILA(seq)
	val inst_ila_hbm = Module(new ila_hbm(Seq(	
		fire_ar_0,

		fire_r_0,

		io.axi(0).ar.valid,

		io.axi(0).ar.ready,
		
		io.axi(0).r.valid,

		io.axi(0).r.ready,


	)))
	inst_ila_hbm.connect(clock)
}