package rps

import chisel3._
import chisel3.util._
import common.axi.AXIS
import common.storage.XQueue
import common.ToZero
import common.ToAllOnes

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
	})

	val risingStart		= io.start===1.U & RegNext(!io.start)
	val sIdle :: sWork :: sEnd :: Nil = Enum(3)

	{//send meta
		val q 						= XQueue(new TX_META, TODO_32)
		q.io.out					<> io.send_meta
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

	{//recv
		val meta					= io.recv_meta
		val data					= io.recv_data
		meta.ready					:= 1.U
		data.ready					:= 1.U
		dontTouch(meta)
		dontTouch(data)
	}

	RPSConters.record(io.send_meta.fire(), "Client_SendMetaFire")
	RPSConters.record(io.send_data.fire(), "Client_SendDataFire")
	RPSConters.record(io.send_data.fire()&io.send_data.bits.last.asBool(), "Client_SendDataLast")
	RPSConters.record(io.recv_meta.fire(), "Client_RecvMetaFire")
	RPSConters.record(io.recv_data.fire(), "Client_RecvDataFire")
	RPSConters.record(io.recv_data.fire()&io.recv_data.bits.last.asBool(), "Client_RecvDataLast")
}