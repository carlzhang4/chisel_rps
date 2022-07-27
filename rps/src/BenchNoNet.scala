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
import common.XCounters

object RPSConters extends XCounters("rps"){
	override def MAX_NUM = 64
} 
//Roce interface
object APP_OP_CODE extends ChiselEnum{
  val APP_READ    = Value
  val APP_WRITE  = Value
  val APP_SEND  = Value
  val reserve1  = Value//note that each  stateClientMeta must be declared, otherwise the cast will warn you
}

class TX_META()extends Bundle{
    val rdma_cmd = APP_OP_CODE()
    val qpn = UInt(24.W)
    val local_vaddr = UInt(48.W)
    val remote_vaddr = UInt(48.W)
    val length = UInt(32.W)
}

class CMPT_META()extends Bundle{
    val qpn         = UInt(16.W)
    val msg_num     = UInt(24.W)  
}

class RECV_META()extends Bundle{
    val qpn         = UInt(16.W)
    val msg_num     = UInt(24.W) 
    val pkg_num     = UInt(21.W) //2G\1K
    val pkg_total   = UInt(21.W)
}

class RoceSim extends Module{
	def TODO_32 		= 32
	def TODO_256		= 256
	def TOTAL_RPCS 		= 1024L*1024*1024
	def MSG_BEATS		= 64 // 4K/64=64
	val io = IO(new Bundle{
		val send_meta	= Flipped(Decoupled(new TX_META))
		val send_data	= Flipped(Decoupled(new AXIS(512)))

		val recv_meta	= Decoupled(new RECV_META)
		val recv_data	= Decoupled(new AXIS(512))

		// val cmpt_meta	= Decoupled(new CMPT_META)

		val start		= Input(UInt(1.W))
		val num_rpcs	= Input(UInt(32.W))
	})

	/*
		* arbiter 0 => client to bs
		* arbiter 1 => cs to bs
		* io.out => user
		* in simlulation, arbiter receive TX_META, transform to RECV_META at out
	*/
	val arbiter						= CompositeArbiter(new TX_META, AXIS(512), 2)
	io.recv_meta.valid				:= arbiter.io.out_meta.valid
	io.recv_meta.ready				<> arbiter.io.out_meta.ready
	io.recv_meta.bits.qpn			:= arbiter.io.out_meta.bits.qpn
	io.recv_meta.bits.pkg_total		:= 1.U
	io.recv_meta.bits.pkg_num		:= 0.U//shoud be ok as mtu is larger than 4K
	io.recv_meta.bits.msg_num		:= 0.U//todo, should be incremental when real
	io.recv_data					<> arbiter.io.out_data

	/*
		* user => io.in
		* router 0 => bs to client
		* router 1 => bs to cs
		* in simlulation, router receive RECV_META(transformed from TX_META) at in
	*/
	val router							= CompositeRouter(new RECV_META, AXIS(512), 2)
	router.io.in_meta.valid				:= io.send_meta.valid
	router.io.in_meta.ready				<> io.send_meta.ready
	router.io.in_meta.bits.qpn			<> io.send_meta.bits.qpn
	router.io.in_meta.bits.pkg_total	<> 1.U
	router.io.in_meta.bits.pkg_num		<> 0.U
	router.io.in_meta.bits.msg_num		<> 0.U
	router.io.in_data					<> io.send_data
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

	client.io.start		:= io.start
	client.io.num_rpcs	:= io.num_rpcs
	client.io.send_meta	<> arbiter.io.in_meta(0)
	client.io.send_data	<> arbiter.io.in_data(0)
	client.io.recv_meta	<> router.io.out_meta(0)
	client.io.recv_data	<> router.io.out_data(0)

	cs.io.send_meta		<> arbiter.io.in_meta(1)
	cs.io.send_data		<> arbiter.io.in_data(1)
	cs.io.recv_meta		<> router.io.out_meta(1)
	cs.io.recv_data		<> router.io.out_data(1)
}
class BenchNetSim(NumChannels:Int=4, Factor:Int=12) extends Module{
	val io = IO(new Bundle{
		val axi_hbm					= Vec(NumChannels,AXI_HBM())
		val axib					= Flipped(new AXIB)

		val start					= Input(UInt(1.W))
		val num_rpcs				= Input(UInt(32.W))
		val counters 				= Vec(RPSConters.MAX_NUM, Output(UInt(32.W)))

		val c2h_cmd					= Decoupled(new C2H_CMD)
		val c2h_data				= Decoupled(new C2H_DATA) 
		val h2c_cmd					= Decoupled(new H2C_CMD)
		val h2c_data				= Flipped(Decoupled(new H2C_DATA))
	})

	val qdma_control	= {
		val t = Module(new QDMAControl)
		t.io.axi		<> io.axib
		t.io.c2h_cmd	<> io.c2h_cmd
		t.io.c2h_data	<> io.c2h_data
		t.io.h2c_cmd	<> io.h2c_cmd
		t.io.h2c_data	<> io.h2c_data
		t.io.pfch_tag	<> 0.U//todo
		t.io.tag_index	<> 0.U//todo
		t.io.start_addr	<> 0.U//todo
		t
	}
	
	//generate flow
	val net	= Module(new RoceSim)
	net.io.start						:= io.start
	net.io.num_rpcs						:= io.num_rpcs

	val arbiter_net						= CompositeArbiter(new TX_META, AXIS(512), 2)
	arbiter_net.io.out_meta				<> net.io.send_meta
	arbiter_net.io.out_data				<> net.io.send_data
	val send_meta_client				= arbiter_net.io.in_meta(0)
	val send_data_client				= arbiter_net.io.in_data(0)
	val send_meta_cs					= arbiter_net.io.in_meta(1)
	val send_data_cs					= arbiter_net.io.in_data(1)

	val router_net						= CompositeRouter(new RECV_META, AXIS(512), 2)
	router_net.io.in_meta				<> net.io.recv_meta
	router_net.io.in_data				<> net.io.recv_data
	when(router_net.io.in_meta.bits.qpn === 1.U){
		router_net.io.idx			:= 0.U//client
	}.elsewhen(router_net.io.in_meta.bits.qpn === 2.U){
		router_net.io.idx			:= 1.U//cs
	}.otherwise{
		router_net.io.idx			:= 0.U
	}
	val recv_meta_client			= router_net.io.out_meta(0)
	val recv_data_client			= router_net.io.out_data(0)
	val recv_meta_cs				= router_net.io.out_meta(1)
	val recv_data_cs				= router_net.io.out_data(1)

	//recv from client (qpn=1), send to cs (qpn=2)
	val client_req_handler			= Module(new ClientReqHandler)
	client_req_handler.io.recv_meta	<> recv_meta_client
	client_req_handler.io.recv_data	<> recv_data_client
	client_req_handler.io.send_meta	<> send_meta_cs
	client_req_handler.io.send_data	<> send_data_cs
	
	client_req_handler.io.axi_hbm	<> io.axi_hbm

	//recv from cs (qpn=2), send to client (qpn=1)
	val cs_req_handler				= Module(new CSReqHandler)
	cs_req_handler.io.recv_meta		<> recv_meta_cs
	cs_req_handler.io.recv_data		<> recv_data_cs
	cs_req_handler.io.send_meta		<> send_meta_client
	cs_req_handler.io.send_data		<> send_data_client

	//host communication
	{
		val arbiter						= CompositeArbiter(new Meta2Host,new Data2Host,2)
		arbiter.io.out_meta				<> qdma_control.io.meta2host
		arbiter.io.out_data				<> qdma_control.io.data2host

		client_req_handler.io.meta2host	<> arbiter.io.in_meta(0)
		client_req_handler.io.data2host	<> arbiter.io.in_data(0)

		cs_req_handler.io.meta2host		<> arbiter.io.in_meta(1)
		cs_req_handler.io.data2host		<> arbiter.io.in_data(1)
		arbiter.io.in_meta(1).bits.addr_offset	:= cs_req_handler.io.meta2host.bits.addr_offset

		val router						= SimpleRouter(new MetaFromHost,2)
		router.io.in					<> qdma_control.io.metaFromHost
		when(router.io.in.bits.data(511,480) === 1.U){//send to client_req_handler
			router.io.idx				:= 0.U
		}.elsewhen(router.io.in.bits.data(511,480) === 2.U){//send to cs_req_handler
			router.io.idx				:= 1.U
		}.otherwise{
			router.io.idx				:= 0.U
		}
		router.io.out(0)				<> client_req_handler.io.meta_from_host
		router.io.out(1)				<> cs_req_handler.io.meta_from_host
	}

	val counters		= Wire(Vec(RPSConters.MAX_NUM,UInt(32.W)))
	for(i<- 0 until RPSConters.MAX_NUM){
		counters(i)		:= 0.U
		io.counters(i)	:= counters(i)
	}
	RPSConters.get_counters(counters)
	RPSConters.print_msgs()
	
}