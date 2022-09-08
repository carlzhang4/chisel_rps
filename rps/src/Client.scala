package rps

import chisel3._
import chisel3.util._
import common.axi.AXIS
import common.storage.XQueue
import common.ToZero
import common.ToAllOnes
import common.Collector
import roce.util.TX_META
import roce.util.RECV_META
import roce.util.APP_OP_CODE
import common.Timer
import common.connection.Connection

class Client()extends Module{
	//client send 4KB data out, and recv 64B rps resp, count the latency
	def TODO_32 		= 32
	def MSG_BEATS		= 64 // 4K/64=64
	def PACK_LENGTH		= 4*1024
	val io = IO(new Bundle{
		val start		= Input(UInt(1.W))
		val num_rpcs	= Input(UInt(32.W))

		val send_meta 	= Decoupled(new TX_META)
		val send_data	= Decoupled(AXIS(512))

		val recv_meta	= Flipped(Decoupled(new RECV_META))
		val recv_data	= Flipped(Decoupled(AXIS(512)))

		val en_cycles		= Input(UInt(32.W))
		val total_cycles	= Input(UInt(32.W))
	})

	val risingStart		= io.start===1.U & RegNext(!io.start)
	val sIdle :: sWork :: sEnd :: Nil = Enum(3)

	{//send meta
		val q 						= XQueue(new TX_META, TODO_32)
		// q.io.out					<> io.send_meta
		Connection.limit(q.io.out,io.send_meta,io.en_cycles,io.total_cycles)
		val reg_count				= RegInit(UInt(32.W), 0.U)//count q.io.in.fire()
		val state					= RegInit(sIdle)
		switch(state){
			is(sIdle){
				reg_count	:= 0.U
				when(io.start===1.U){
					state		:= sWork
				}
			}
			is(sWork){
				when(q.io.in.fire && reg_count+1.U===io.num_rpcs){
					state		:= sEnd
				}
			}
			is(sEnd){
				when(risingStart){
					state		:= sIdle
				}
			}
		}
		q.io.in.valid				:= state === sWork
		q.io.in.bits.rdma_cmd		:= APP_OP_CODE.APP_SEND
		q.io.in.bits.qpn			:= 1.U
		q.io.in.bits.length			:= PACK_LENGTH.U
		q.io.in.bits.local_vaddr	:= 0.U
		q.io.in.bits.remote_vaddr	:= 0.U

		when(q.io.in.fire()){
			reg_count				:= reg_count+1.U
		}
	}

	//send data
	{	
		val q							= XQueue(AXIS(512), TODO_32)
		q.io.out						<> io.send_data
		val en							= q.io.in.fire()
		val (counter,wrap)				= Counter(en, MSG_BEATS)
		val reg_count					= RegInit(UInt(32.W), 0.U)//count q.io.in.bits.last
		val state						= RegInit(sIdle)
		switch(state){
			is(sIdle){
				reg_count				:= 0.U
				when(io.start===1.U){
					state				:= sWork
				}
			}
			is(sWork){
				when(wrap && reg_count+1.U===io.num_rpcs){
					state		:= sEnd
				}
			}
			is(sEnd){
				when(risingStart){
					state		:= sIdle
				}
			}
		}
		ToAllOnes(q.io.in.bits.keep)
		q.io.in.valid				:= state === sWork
		q.io.in.bits.data			:= reg_count
		q.io.in.bits.last			:= wrap	
		when(wrap){
			reg_count				:= reg_count+1.U
		}
	}

	val reg_count_recv_meta = {//recv
		val reg_count_meta			= RegInit(UInt(32.W),0.U)
		val meta					= io.recv_meta
		val data					= io.recv_data
		meta.ready					:= 1.U
		data.ready					:= 1.U

		when(risingStart){
			reg_count_meta			:= 0.U
		}.otherwise{
			when(meta.fire()){
				reg_count_meta		:= reg_count_meta+1.U
			}
		}
		reg_count_meta
	}

	val reg_first_send	= RegInit(Bool(),false.B)
	when(io.send_meta.fire()){
		reg_first_send	:= true.B
	}.otherwise{
		reg_first_send	:= reg_first_send
	}
	val begin			= io.send_meta.fire() & !reg_first_send
	val end				= reg_count_recv_meta+1.U===io.num_rpcs & io.recv_meta.fire()
	val total_cycles	= Timer(begin, end).latency


	val e2e_timer = Timer(io.send_meta.fire(),io.recv_meta.fire())
	val e2e_latency = e2e_timer.latency
	val e2e_start_cnt = e2e_timer.cnt_start
	val e2e_end_cnt = e2e_timer.cnt_end

	Collector.fire(io.send_meta)
	Collector.fire(io.send_data)
	Collector.fire(io.recv_meta)
	Collector.fire(io.recv_data)

	Collector.report(e2e_latency,fix_str = "REG_E2E_LATENCY")

	Collector.report(total_cycles,fix_str = "REG_TOTAL_CYCLES")
}