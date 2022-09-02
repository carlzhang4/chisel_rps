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
import roce.util.TX_META
import roce.util.RECV_META
import roce.util.APP_OP_CODE
import common.Collector
import common.Timer

class CSReqHandler extends Module{
	def TODO_32 = 32
	def CS_HOST_MEM_OFFSET = 1024L*1024*1024
	def HOST_MEM_PARTITION = 1024L*1024*1024 //client req and cs req
	val io = IO(new Bundle{
		val recv_meta		= Flipped(Decoupled(new RECV_META))
		val recv_data		= Flipped(Decoupled(AXIS(512)))

		val meta_from_host	= Flipped(Decoupled(new MetaFromHost))
		val meta2host		= Decoupled(new Meta2Host)
		val data2host		= Decoupled(new Data2Host)

		val send_meta		= Decoupled(new TX_META)
		val send_data		= Decoupled(AXIS(512))
	})
	io.recv_data.ready		:= 1.U//not used

	val cs_req_qdma_latency = Timer(io.meta2host.fire(), io.meta_from_host.fire()).latency
	Collector.report(cs_req_qdma_latency,fix_str = "REG_CS_REQ_QDMA_LATENCY")

	val q_2host_meta						= XQueue(new Meta2Host, TODO_32)
	val q_2host_data						= XQueue(new Data2Host, TODO_32)
	val reg_offset							= RegInit(UInt(48.W),0.U)

	Connection.one2many(io.recv_meta)(q_2host_meta.io.in, q_2host_data.io.in)

	q_2host_meta.io.in.bits.addr_offset		:= (reg_offset%HOST_MEM_PARTITION.U) + CS_HOST_MEM_OFFSET.U
	q_2host_meta.io.in.bits.len				:= 64.U
	q_2host_data.io.in.bits.data			:= reg_offset + CS_HOST_MEM_OFFSET.U + 1.U
	q_2host_data.io.in.bits.last			:= 1.U

	q_2host_meta.io.out						<> io.meta2host
	q_2host_data.io.out						<> io.data2host

	when(q_2host_meta.io.in.fire()){//when meta fires, data fires too
		reg_offset							:= reg_offset+64.U
	}

	Connection.one2many(io.meta_from_host)(io.send_meta, io.send_data)
	io.send_meta.bits.rdma_cmd			:= APP_OP_CODE.APP_SEND
	io.send_meta.bits.qpn				:= 1.U
	io.send_meta.bits.length				:= 64.U
	io.send_meta.bits.local_vaddr		:= 0.U
	io.send_meta.bits.remote_vaddr		:= 0.U

	val reg_count						= RegInit(UInt(32.W),0.U)
	ToAllOnes(io.send_data.bits)
	io.send_data.bits.data				:= reg_count
	io.send_data.bits.last				:= 1.U

	when(io.send_data.fire()){
		reg_count					:= reg_count+1.U
	}

	Collector.fire(io.recv_meta)
	Collector.fire(io.recv_data)
	Collector.fireLast(io.recv_data)
	Collector.fire(io.meta2host)
	Collector.fire(io.data2host)
	Collector.fireLast(io.data2host)
	Collector.fire(io.meta_from_host)
	Collector.fire(io.send_meta)
	Collector.fire(io.send_data)
	Collector.fireLast(io.send_data)
}