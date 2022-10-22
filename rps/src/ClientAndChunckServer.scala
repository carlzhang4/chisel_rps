package rps

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum
import common.axi.AXI_HBM
import qdma.AXIB
import qdma.H2C_CMD
import qdma.H2C_DATA
import qdma.C2H_CMD
import qdma.C2H_DATA
import common.axi.AXIS
import common.storage.XQueue
import common.connection.CompositeArbiter
import common.connection.CompositeRouter
import common.connection.Connection
import common.ToZero
import common.ToAllOnes
import common.storage.RegSlice
import common.connection.SerialRouter
import common.connection.SimpleRouter
import network.roce.util.TX_META
import network.roce.util.RECV_META
import common.Collector
import common.Timer
import common.BaseILA



class ClientAndChunckServer extends Module{
	def TODO_32 		= 32
	def TODO_256		= 256
	def MSG_BEATS		= 64 // 4K/64=64
	val io = IO(new Bundle{
		val send_meta		= Decoupled(new TX_META)
		val send_data		= Decoupled(new AXIS(512))

		val recv_meta		= Flipped(Decoupled(new RECV_META))
		val recv_data		= Flipped(Decoupled(new AXIS(512)))

		val start			= Input(UInt(1.W))
		val num_rpcs		= Input(UInt(32.W))
		val en_cycles		= Input(UInt(32.W))
		val total_cycles	= Input(UInt(32.W))
	})

	/*
		* arbiter 0 => client to bs
		* arbiter 1 => cs to bs
		* io.out => send to net
	*/
	val arbiter						= CompositeArbiter(new TX_META, AXIS(512), 2)
	io.send_meta					<> arbiter.io.out_meta
	io.send_data					<> arbiter.io.out_data

	// class ila_client(seq:Seq[Data]) extends BaseILA(seq)
	// 	val inst_client = Module(new ila_client(Seq(	
	// 		io.send_meta,
	// 		io.send_data,
	// 	)))
	// inst_client.connect(clock)

	/*
		* recv from net => io.in
		* router 0 => bs to client
		* router 1 => bs to cs
	*/
	val router							= CompositeRouter(new RECV_META, AXIS(512), 2)
	router.io.in_meta					<> io.recv_meta
	router.io.in_data					<> io.recv_data
	when(router.io.in_meta.bits.qpn === 1.U){
		router.io.idx			:= 0.U//client
	}.elsewhen(router.io.in_meta.bits.qpn === 2.U){
		router.io.idx			:= 1.U//cs
	}.otherwise{
		router.io.idx			:= 0.U
	}

	val sIdle :: sWork :: sEnd :: Nil = Enum(3)
	
	val client			= Module(new Client)
	val cs				= Module(new ChunckServer)

	client.io.start			:= io.start
	client.io.num_rpcs		:= io.num_rpcs
	client.io.en_cycles		:= io.en_cycles
	client.io.total_cycles	:= io.total_cycles
	client.io.send_meta		<> arbiter.io.in_meta(0)
	client.io.send_data		<> arbiter.io.in_data(0)
	client.io.recv_meta		<> router.io.out_meta(0)
	client.io.recv_data		<> router.io.out_data(0)

	cs.io.send_meta			<> arbiter.io.in_meta(1)
	cs.io.send_data			<> arbiter.io.in_data(1)
	cs.io.recv_meta			<> router.io.out_meta(1)
	cs.io.recv_data			<> router.io.out_data(1)


	val phase1_latency = Timer(client.io.send_meta.fire(), cs.io.recv_meta.fire()).latency
	val phase2_latency = Timer(cs.io.send_meta.fire(), client.io.recv_meta.fire()).latency
	val cs_e2e_latency = Timer(cs.io.recv_meta.fire(),cs.io.send_meta.fire()).latency

	Collector.report(phase1_latency,fix_str = "REG_PHASE1_LATENCY")
	Collector.report(phase2_latency,fix_str = "REG_PHASE2_LATENCY")
	Collector.report(cs_e2e_latency,fix_str = "CS_E2E_LATENCY")

}