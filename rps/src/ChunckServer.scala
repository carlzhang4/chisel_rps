package rps

import chisel3._
import chisel3.util._
import common.axi.AXIS
import common.storage.XQueue
import common.storage.RegSlice
import common.connection.Connection
import common.ToAllOnes
import roce.util.TX_META
import roce.util.RECV_META
import roce.util.APP_OP_CODE
import common.Collector

class ChunckServer extends Module{
	def TODO_CS_CYCLES = 256
	val io = IO(new Bundle{
		val send_meta 	= Decoupled(new TX_META)
		val send_data	= Decoupled(AXIS(512))

		val recv_meta	= Flipped(Decoupled(new RECV_META))
		val recv_data	= Flipped(Decoupled(AXIS(512)))
	})

	val sIdle :: sWork :: sEnd :: Nil = Enum(3)
	
	io.recv_data.ready	:= 1.U//todo discard data

	val recv_meta_delay	= RegSlice(TODO_CS_CYCLES)(io.recv_meta)
	Connection.one2many(recv_meta_delay)(io.send_data, io.send_meta)
	io.send_meta.bits.rdma_cmd		:= APP_OP_CODE.APP_SEND
	io.send_meta.bits.qpn			:= 2.U
	io.send_meta.bits.length		:= 64.U
	io.send_meta.bits.local_vaddr	:= 0.U
	io.send_meta.bits.remote_vaddr	:= 0.U

	val reg_count					= RegInit(UInt(32.W),0.U)

	ToAllOnes(io.send_data.bits)
	io.send_data.bits.last			:= 1.U
	io.send_data.bits.data			:= reg_count

	when(io.send_data.fire()){
		reg_count					:= reg_count+1.U
	}

	Collector.fire(io.send_meta)
	Collector.fire(io.send_data)
	Collector.fire(io.recv_meta)
	Collector.fire(io.recv_data)
}