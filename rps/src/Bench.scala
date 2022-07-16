package rps

import chisel3._
import chisel3.util._
import common.axi.AXI_HBM
import common.ToAllOnes
import common.BaseVIO
import common.BaseILA

class HBMCompress(NumChannels:Int=4,Factor:Int=4) extends Module{
	def init_size_byte = (256L*1024*1024*NumChannels)
	val io = IO(new Bundle{
		val axi 			= Vec(NumChannels,AXI_HBM())
		// val start 			= Input(UInt(1.W))
		// val start_compress	= Input(UInt(1.W))
		// val total_cmds		= Input(UInt(32.W))
	})

	for(i<-0 until NumChannels){
		io.axi(i).hbm_init()
	}

	val start = RegInit(UInt(1.W),0.U)//io.start
	val risingStart = start===1.U & RegNext(!start)

	//init HBM
	val aw 				= io.axi(0).aw
	val w 				= io.axi(0).w
	val reg_aw_addr 	= RegInit(UInt(33.W),0.U)
	val aw_bytes		= ((aw.bits.len+&1.U)<<5)
	aw.bits.addr		:= reg_aw_addr

	val reg_w_data			= RegInit(UInt(32.W),0.U)
	val reg_w_parity		= RegInit(UInt(1.W),0.U)
	val reg_w_count_data	= RegInit(UInt(33.W),0.U)
	when(reg_w_parity===0.U){
		w.bits.data		:= reg_w_data
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
			reg_aw_addr			:= 0.U
		}
		is(sWrite){
			when(aw.fire() && reg_aw_addr+aw_bytes === init_size_byte.U){
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
		reg_aw_addr	:= reg_aw_addr + aw_bytes
	}

	switch(state_w){
		is(sIdle){
			when(start===1.U){
				state_w		:= sWrite
			}
			reg_w_count_data	:= 0.U
			reg_w_data		:= 0.U
			reg_w_parity		:= 0.U
		}
		is(sWrite){
			when(w.fire() && reg_w_count_data+32.U === init_size_byte.U){
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
	w.bits.last	:= (reg_w_count_data+32.U)%(aw_bytes)===0.U
	when(w.fire()){
		reg_w_count_data	:= reg_w_count_data + 32.U
		reg_w_parity		:= reg_w_parity + 1.U
		when(reg_w_parity === 1.U){
			reg_w_data	:= reg_w_data + 1.U
		}
	}
	
	val initDone = state_w===sEnd & state_aw===sEnd
	dontTouch(initDone)

	//compress start
	val total_cmds = Wire(UInt(32.W))//io.total_cmds
	val compressorHBM = Module(new CompressorHBM(NumChannels,Factor))

	for(i<-0 until NumChannels){
		compressorHBM.io.hbm_ar(i)	<> io.axi(i).ar
		compressorHBM.io.hbm_r(i)	<> io.axi(i).r
	}

	val cmd_in				= compressorHBM.io.cmd_in
	val startCompress 		= RegInit(UInt(1.W),0.U)//io.start_compress
	val risingStartCMD		= startCompress===1.U & RegNext(!startCompress)
	val reg_cmd_count		= RegInit(UInt(32.W),0.U)
	val reg_cmd_addr		= RegInit(UInt(33.W),0.U)
	val state_cmd 			= RegInit(sIdle)
	switch(state_cmd){
		is(sIdle){
			when(startCompress === 1.U){
				state_cmd	:= sWrite
			}
			reg_cmd_count		:= 0.U
			reg_cmd_addr		:= 0.U
		}
		is(sWrite){
			when(cmd_in.fire() && reg_cmd_count+1.U===total_cmds){
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
	cmd_in.bits.addr 	:= reg_cmd_addr
	when(cmd_in.fire()){
		reg_cmd_count	:= reg_cmd_count+1.U

		when((reg_cmd_count+1.U)%NumChannels.U === 0.U){
			reg_cmd_addr	:= reg_cmd_addr + 4096.U - (256L*1024*1024*(NumChannels-1)).U
		}.otherwise{
			reg_cmd_addr	:= reg_cmd_addr + (256L*1024*1024).U
		}
	}

	val state_out			= RegInit(sIdle)
	val out					= compressorHBM.io.out
	val reg_out_count 		= RegInit(UInt(32.W),0.U)
	val reg_out_count_last	= RegInit(UInt(32.W),0.U)
	switch(state_out){
		is(sIdle){
			when(startCompress===1.U){
				state_out		:= sWrite
			}
			reg_out_count			:= 0.U
			reg_out_count_last	:= 0.U
		}
		is(sWrite){
			when(out.fire() && out.bits.last===1.U && reg_out_count_last+1.U===total_cmds){
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
		reg_out_count	:= reg_out_count+1.U
		when(out.bits.last===1.U){
			reg_out_count_last	:= reg_out_count_last+1.U
		}
	}
	
	val reg_count_time 	= RegInit(UInt(32.W),0.U)
	when(startCompress === 1.U){
		when(reg_out_count_last === total_cmds){
			reg_count_time	:= reg_count_time
		}.otherwise{
			reg_count_time	:= reg_count_time + 1.U
		}
	}.otherwise{
		reg_count_time		:= 0.U
	}
	dontTouch(reg_count_time)

	class vio_compress(seq:Seq[Data]) extends BaseVIO(seq)
	val mod_vio_compress = Module(new vio_compress(Seq(
		start,
		startCompress,
		total_cmds,
	)))
	mod_vio_compress.connect(clock)

	val out_data = out.bits.data(63,0)

	class ila_compress(seq:Seq[Data]) extends BaseILA(seq)
	val inst = Module(new ila_compress(Seq(	
		reg_aw_addr,
		reg_w_data,
		reg_w_count_data,
		reg_cmd_count,
		reg_cmd_addr,
		reg_out_count,
		reg_out_count_last,
		reg_count_time,
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
	val fire_r_0	= record_signals(io.axi(0).r.fire())

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