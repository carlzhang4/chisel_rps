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

class BlockServer(NumChannels:Int=4, Factor:Int=12, IsDummy:Boolean=false, Index:Int=0) extends Module{
	val io = IO(new Bundle{
		val axi_hbm					= if(!IsDummy) Some(Vec(NumChannels,AXI_HBM())) else None
		val axib					= Flipped(new AXIB)

		val start_addr				= Input(UInt(64.W))
		val pfch_tag				= Input(UInt(32.W))
		val tag_index				= Input(UInt(32.W))

		val c2h_cmd					= Decoupled(new C2H_CMD)
		val c2h_data				= Decoupled(new C2H_DATA) 
		val h2c_cmd					= Decoupled(new H2C_CMD)
		val h2c_data				= Flipped(Decoupled(new H2C_DATA))

		val send_meta				= Decoupled(new TX_META)
		val send_data				= Decoupled(new AXIS(512))

		val recv_meta				= Flipped(Decoupled(new RECV_META))
		val recv_data				= Flipped(Decoupled(new AXIS(512)))
	})

	Collector.fire(io.recv_meta)
	Collector.fire(io.recv_data)
	Collector.fireLast(io.recv_data)
	Collector.report(io.recv_meta.valid)
	Collector.report(io.recv_meta.ready)
	Collector.report(io.recv_data.valid)
	Collector.report(io.recv_data.ready)
	Collector.report(io.c2h_data.valid)
	Collector.report(io.c2h_data.ready)

	val qdma_control	= {
		val t = Module(new QDMAControl)
		t.io.axi		<> io.axib
		t.io.c2h_cmd	<> io.c2h_cmd
		t.io.c2h_data	<> io.c2h_data
		t.io.h2c_cmd	<> io.h2c_cmd
		t.io.h2c_data	<> io.h2c_data
		t.io.pfch_tag	<> io.pfch_tag
		t.io.tag_index	<> io.tag_index
		t.io.start_addr	<> io.start_addr
		t
	}

	val arbiter_net						= CompositeArbiter(new TX_META, AXIS(512), 2)
	arbiter_net.io.out_meta				<> io.send_meta
	arbiter_net.io.out_data				<> io.send_data
	val send_meta_client				= arbiter_net.io.in_meta(0)
	val send_data_client				= arbiter_net.io.in_data(0)
	val send_meta_cs					= arbiter_net.io.in_meta(1)
	val send_data_cs					= arbiter_net.io.in_data(1)

	val router_net						= CompositeRouter(new RECV_META, AXIS(512), 2)
	router_net.io.in_meta				<> io.recv_meta
	router_net.io.in_data				<> io.recv_data
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
	val client_req_handler	= IsDummy match {
		case true	=> Module(new DummyClientReqHandler)	
		case false	=> 
			val t = Module(new ClientReqHandler(NumChannels,Factor,Index))
			t.io.axi_hbm	<> io.axi_hbm.get
			t
	}

	client_req_handler.io.recv_meta	<> recv_meta_client
	client_req_handler.io.recv_data	<> recv_data_client
	client_req_handler.io.send_meta	<> send_meta_cs
	client_req_handler.io.send_data	<> send_data_cs

	//recv from cs (qpn=2), send to client (qpn=1)
	val cs_req_handler				= Module(new CSReqHandler)
	cs_req_handler.io.recv_meta		<> recv_meta_cs
	cs_req_handler.io.recv_data		<> recv_data_cs
	cs_req_handler.io.send_meta		<> send_meta_client
	cs_req_handler.io.send_data		<> send_data_client

	//host communication
	{
		val arbiter						= CompositeArbiter(new WriteCMD,new WriteData,2)
		arbiter.io.out_meta				<> qdma_control.io.writeCMD
		arbiter.io.out_data				<> qdma_control.io.writeData

		client_req_handler.io.writeCMD	<> arbiter.io.in_meta(0)
		client_req_handler.io.writeData	<> arbiter.io.in_data(0)

		cs_req_handler.io.writeCMD		<> arbiter.io.in_meta(1)
		cs_req_handler.io.writeData		<> arbiter.io.in_data(1)
		arbiter.io.in_meta(1).bits.addr_offset	:= cs_req_handler.io.writeCMD.bits.addr_offset

		val router						= SimpleRouter(new AxiBridgeData,2)
		router.io.in					<> qdma_control.io.axib_data
		when(router.io.in.bits.data(511,480) === 1.U){//send to client_req_handler
			router.io.idx				:= 0.U
		}.elsewhen(router.io.in.bits.data(511,480) === 2.U){//send to cs_req_handler
			router.io.idx				:= 1.U
		}.otherwise{
			router.io.idx				:= 0.U
		}
		router.io.out(0)				<> client_req_handler.io.axib_data
		router.io.out(1)				<> cs_req_handler.io.axib_data

		qdma_control.io.readCMD			<> client_req_handler.io.readCMD
		qdma_control.io.readData		<> client_req_handler.io.readData

		Collector.fireLast(arbiter.io.in_data(0))
		Collector.fire(arbiter.io.in_data(0))
		Collector.fire(arbiter.io.in_meta(0))
		Collector.fireLast(arbiter.io.in_data(1))
		Collector.fire(arbiter.io.in_data(1))
		Collector.fire(arbiter.io.in_meta(1))
		Collector.fireLast(arbiter.io.out_data)
		Collector.fire(arbiter.io.out_data)
		Collector.fire(arbiter.io.out_meta)
	}

	val bs_req_process_latency = Timer(client_req_handler.io.recv_meta.fire(), client_req_handler.io.send_meta.fire()).latency
	val cs_req_process_latency = Timer(cs_req_handler.io.recv_meta.fire(), cs_req_handler.io.send_meta.fire()).latency

	Collector.report(bs_req_process_latency,fix_str = "REG_BS_REQ_PROCESS_LATENCY")
	Collector.report(cs_req_process_latency,fix_str = "REG_CS_REQ_PROCESS_LATENCY")
	
}