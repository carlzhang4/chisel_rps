package rps

import chisel3._
import chisel3.util._
import common._
import common.storage.XAXIConverter
import common.storage.XQueue
import hbm._
import qdma.QDMAPin
import qdma.QDMA
import qdma.SimpleAXISlave
import qdma.AXIB
import qdma.H2C
import qdma.H2CLatency
import qdma.C2HLatency
import network.cmac.XCMAC
import network.cmac.CMACPin
import network.NetworkStack
import common.storage.RegSlice
import common.storage.XConverter
import common.storage.XQueueConfig
import common.storage.AXIRegSlice
import common.connection.CompositeArbiter
import qdma.C2H_CMD
import qdma.C2H_DATA
import common.connection.CompositeRouter
import common.connection.SimpleRouter

object GlobalConfig{
	var ChannelOffset	= 0
	var ChannelOffset_1	= 0
}
class RPSTop extends RawModule {
	val qdma_pin		= IO(new QDMAPin())
	val cmac_pin		= IO(new CMACPin)
	val cmac_pin1		= IO(new CMACPin)
	val sysClkP			= IO(Input(Clock()))
	val sysClkN			= IO(Input(Clock()))
	val led		        = IO(Output(UInt(1.W)))

	led         		:= 0.U

	def TODO_32			= 32
	
	val mmcmTop	= Module(new MMCME4_ADV_Wrapper(
		CLKFBOUT_MULT_F 		= 12,
		MMCM_DIVCLK_DIVIDE		= 1,
		MMCM_CLKOUT0_DIVIDE_F	= 12,
		MMCM_CLKOUT1_DIVIDE_F	= 4,
		MMCM_CLKOUT2_DIVIDE_F	= 12,
		MMCM_CLKOUT3_DIVIDE_F	= 5,
		MMCM_CLKIN1_PERIOD 		= 10
	))

	mmcmTop.io.CLKIN1	:= IBUFDS(sysClkP,sysClkN)
	mmcmTop.io.RST		:= 0.U

	val clk_hbm_driver	= mmcmTop.io.CLKOUT0 //100M
	val userClk			= mmcmTop.io.CLKOUT1 //300M
	val cmacClk			= mmcmTop.io.CLKOUT2 //100M
	val netClk			= mmcmTop.io.CLKOUT3 //240M

	//HBM
    val hbmDriver 		= withClockAndReset(clk_hbm_driver, false.B) {Module(new HBM_DRIVER(WITH_RAMA=false))}
	hbmDriver.getTCL()
	val hbmClk 	    	= hbmDriver.io.hbm_clk
	val hbmRstn     	= withClockAndReset(hbmClk,false.B) {RegNext(hbmDriver.io.hbm_rstn.asBool)}
    
	val userRstn		= withClockAndReset(userClk,false.B) {ShiftRegister(hbmRstn,4)}
	val netRstn			= withClockAndReset(netClk,false.B) {ShiftRegister(hbmRstn,4)}

	for (i <- 0 until 32) {
		hbmDriver.io.axi_hbm(i).hbm_init()	// Read hbm_init function if you're not familiar with AXI.
	}

	dontTouch(hbmClk)
	dontTouch(hbmRstn)
	//QDMA
	val qdma 			= Module(new QDMA("202101"))
	qdma.getTCL()
	ToZero(qdma.io.reg_status)
	ToZero(qdma.io.c2h_data.bits)
	ToZero(qdma.io.h2c_cmd.bits)
	ToZero(qdma.io.c2h_cmd.bits)
	qdma.io.h2c_data.ready	:= 0.U
	qdma.io.c2h_data.valid	:= 0.U
	qdma.io.h2c_cmd.valid	:= 0.U
	qdma.io.c2h_cmd.valid	:= 0.U

	qdma.io.pin <> qdma_pin
	qdma.io.user_clk	:= userClk
	qdma.io.user_arstn	:= userRstn

	val control_reg = qdma.io.reg_control
	val status_reg = qdma.io.reg_status

	val sw_reset	= control_reg(100) === 1.U


	val roce								= Module(new NetworkStack)
	val roce1								= Module(new NetworkStack(IS_PART_1=true))

	roce.io.pin								<> cmac_pin
	roce.io.user_clk						:= netClk
	roce.io.user_arstn						:= netRstn
	roce.io.sys_reset						:= !netRstn
	roce.io.drp_clk							:= cmacClk
	roce.io.m_mem_read_cmd.ready			:= 1.U
	roce.io.m_mem_write_cmd.ready			:= 1.U
	roce.io.m_mem_write_data.ready			:= 1.U
	roce.io.s_mem_read_data.valid			:= 0.U
	roce.io.m_cmpt_meta.ready				:= 1.U
	roce.io.arp_rsp.ready					:= 1.U
	roce.io.arp_req.valid					:= 0.U
	roce.io.sw_reset						:= sw_reset
	ToZero(roce.io.s_mem_read_data.bits)
	ToZero(roce.io.qp_init.bits)
	
	roce1.io.pin							<> cmac_pin1
	roce1.io.user_clk						:= netClk
	roce1.io.user_arstn						:= netRstn
	roce1.io.sys_reset						:= !netRstn
	roce1.io.drp_clk						:= cmacClk
	roce1.io.m_mem_read_cmd.ready			:= 1.U
	roce1.io.m_mem_write_cmd.ready			:= 1.U
	roce1.io.m_mem_write_data.ready			:= 1.U
	roce1.io.s_mem_read_data.valid			:= 0.U
	roce1.io.m_cmpt_meta.ready				:= 1.U
	roce1.io.arp_rsp.ready					:= 1.U
	roce1.io.arp_req.valid					:= 0.U
	roce1.io.sw_reset						:= sw_reset
	ToZero(roce1.io.s_mem_read_data.bits)
	ToZero(roce1.io.qp_init.bits)

	roce.io.qp_init.bits.remote_udp_port	:= 17.U	
	roce1.io.qp_init.bits.remote_udp_port	:= 17.U	
		

	withClockAndReset(netClk, sw_reset || !netRstn){
		val start 							= RegNext(control_reg(101) === 1.U)
		val risingStartInit					= start && RegNext(!start)
		val valid 							= RegInit(UInt(1.W),0.U)
		when(risingStartInit === 1.U){
			valid							:= 1.U
		}.elsewhen(roce.io.qp_init.fire()){
			valid							:= 0.U
		}
		roce.io.qp_init.valid				:= valid
		roce.io.qp_init.bits.remote_ip		:= 0x51bda8c0.U
		roce.io.ip_address					:= 0x52bda8c0.U//0x01bda8c0 01/189/168/192
		roce.io.qp_init.bits.credit			:= control_reg(103)

		val cur_qp							= RegInit(UInt(1.W),0.U)
		when(roce.io.qp_init.fire()){
			cur_qp							:= cur_qp + 1.U
		}

		when(cur_qp === 0.U){
			roce.io.qp_init.bits.qpn			:= 1.U
			roce.io.qp_init.bits.remote_qpn		:= 1.U
			roce.io.qp_init.bits.local_psn		:= 0x2001.U
			roce.io.qp_init.bits.remote_psn		:= 0x1001.U
		}.otherwise{
			roce.io.qp_init.bits.qpn			:= 2.U
			roce.io.qp_init.bits.remote_qpn		:= 2.U
			roce.io.qp_init.bits.local_psn		:= 0x2002.U
			roce.io.qp_init.bits.remote_psn		:= 0x1002.U
		}

		val valid_arp						= RegInit(UInt(1.W),0.U)
		when(risingStartInit === 1.U){
			valid_arp						:= 1.U
		}.elsewhen(roce.io.arp_req.fire()){
			valid_arp						:= 0.U
		}
		roce.io.arp_req.valid				:= valid_arp
		roce.io.arp_req.bits				:= 0x51bda8c0.U
	}

	withClockAndReset(netClk, sw_reset || !netRstn){
		val start 							= RegNext(control_reg(101) === 1.U)
		val risingStartInit					= start && RegNext(!start)
		val valid 							= RegInit(UInt(1.W),0.U)
		when(risingStartInit === 1.U){
			valid							:= 1.U
		}.elsewhen(roce1.io.qp_init.fire()){
			valid							:= 0.U
		}
		roce1.io.qp_init.valid				:= valid
		roce1.io.qp_init.bits.remote_ip		:= 0x61bda8c0.U
		roce1.io.ip_address					:= 0x62bda8c0.U//0x01bda8c0 01/189/168/192
		roce1.io.qp_init.bits.credit		:= control_reg(103)

		val cur_qp							= RegInit(UInt(1.W),0.U)
		when(roce1.io.qp_init.fire()){
			cur_qp							:= cur_qp + 1.U
		}

		when(cur_qp === 0.U){
			roce1.io.qp_init.bits.qpn			:= 1.U
			roce1.io.qp_init.bits.remote_qpn	:= 1.U
			roce1.io.qp_init.bits.local_psn		:= 0x4001.U
			roce1.io.qp_init.bits.remote_psn	:= 0x3001.U
		}.otherwise{
			roce1.io.qp_init.bits.qpn			:= 2.U
			roce1.io.qp_init.bits.remote_qpn	:= 2.U
			roce1.io.qp_init.bits.local_psn		:= 0x4002.U
			roce1.io.qp_init.bits.remote_psn	:= 0x3002.U
		}

		val valid_arp						= RegInit(UInt(1.W),0.U)
		when(risingStartInit === 1.U){
			valid_arp						:= 1.U
		}.elsewhen(roce1.io.arp_req.fire()){
			valid_arp						:= 0.U
		}
		roce1.io.arp_req.valid				:= valid_arp
		roce1.io.arp_req.bits				:= 0x61bda8c0.U
	}

	GlobalConfig.ChannelOffset		= 16
	GlobalConfig.ChannelOffset_1	= 20
	val bench	= withClockAndReset(userClk, sw_reset || !userRstn){Module(new BlockServer(4,12,false,0))}
	val bench1	= withClockAndReset(userClk, sw_reset || !userRstn){Module(new BlockServer(4,12,false,1))}

	withClockAndReset(userClk,!userRstn){
		val arbiter	= CompositeArbiter(new C2H_CMD,new C2H_DATA,2)
		arbiter.io.out_meta	<> qdma.io.c2h_cmd
		arbiter.io.out_data	<> qdma.io.c2h_data

		arbiter.io.in_meta(0)	<> bench.io.c2h_cmd
		arbiter.io.in_data(0)	<> bench.io.c2h_data
		arbiter.io.in_meta(1)	<> bench1.io.c2h_cmd
		arbiter.io.in_data(1)	<> bench1.io.c2h_data

		val router				= SimpleRouter(chiselTypeOf(qdma.io.axib.w.bits),2)
		router.io.in			<> XConverter(qdma.io.axib.w,qdma.io.pcie_clk,qdma.io.pcie_arstn,userClk)
		when(router.io.in.bits.data(479,448) === 0.U){
			router.io.idx		:= 0.U
		}.otherwise{
			router.io.idx		:= 1.U
		}
		router.io.out(0)		<> bench.io.axib.w
		router.io.out(1)		<> bench1.io.axib.w

		Init(qdma.io.axib.b)
		qdma.io.axib.b.valid		:= 1.U
		Init(qdma.io.axib.ar)
		Init(qdma.io.axib.r)
		Init(qdma.io.axib.aw)

		Init(bench.io.axib.b)
		Init(bench.io.axib.ar)
		Init(bench.io.axib.r)
		Init(bench.io.axib.aw)

		Init(bench1.io.axib.b)
		Init(bench1.io.axib.ar)
		Init(bench1.io.axib.r)
		Init(bench1.io.axib.aw)
	
	}

	hbmDriver.io.axi_hbm(16) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench.io.axi_hbm.get(0), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(17) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench.io.axi_hbm.get(1), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(18) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench.io.axi_hbm.get(2), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(19) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench.io.axi_hbm.get(3), userClk, userRstn, hbmClk, hbmRstn))}

	hbmDriver.io.axi_hbm(20) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench1.io.axi_hbm.get(0), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(21) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench1.io.axi_hbm.get(1), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(22) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench1.io.axi_hbm.get(2), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(23) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench1.io.axi_hbm.get(3), userClk, userRstn, hbmClk, hbmRstn))}

	bench.io.start_addr			:= Cat(control_reg(110), control_reg(111))
	bench.io.pfch_tag			:= control_reg(113)
	bench.io.tag_index			:= control_reg(114)
	// bench.io.c2h_cmd			<> qdma.io.c2h_cmd
	// bench.io.c2h_data		<> qdma.io.c2h_data
	bench.io.h2c_cmd			<> qdma.io.h2c_cmd
	bench.io.h2c_data			<> qdma.io.h2c_data
	// bench.io.axib 			<> XAXIConverter(qdma.io.axib, qdma.io.pcie_clk, qdma.io.pcie_arstn, qdma.io.user_clk, qdma.io.user_arstn)
	bench.io.recv_meta			<> XConverter(roce.io.m_recv_meta,netClk,netRstn,userClk)
	bench.io.recv_data			<> XConverter(roce.io.m_recv_data,netClk,netRstn,userClk)
	roce.io.s_tx_meta			<> XConverter(bench.io.send_meta,userClk,userRstn,netClk)
	roce.io.s_send_data			<> XConverter(bench.io.send_data,userClk,userRstn,netClk)

	bench1.io.start_addr		:= Cat(control_reg(115), control_reg(116))
	bench1.io.pfch_tag			:= control_reg(113)
	bench1.io.tag_index			:= control_reg(114)
	// bench1.io.c2h_cmd		<> qdma.io.c2h_cmd
	// bench1.io.c2h_data		<> qdma.io.c2h_data
	bench1.io.h2c_cmd.ready		:= 0.U
	bench1.io.h2c_data.valid	:= 0.U
	ToZero(bench1.io.h2c_data.bits)
	// bench1.io.axib 			<> XAXIConverter(qdma.io.axib, qdma.io.pcie_clk, qdma.io.pcie_arstn, qdma.io.user_clk, qdma.io.user_arstn)
	bench1.io.recv_meta			<> XConverter(roce1.io.m_recv_meta,netClk,netRstn,userClk)
	bench1.io.recv_data			<> XConverter(roce1.io.m_recv_data,netClk,netRstn,userClk)
	roce1.io.s_tx_meta			<> XConverter(bench1.io.send_meta,userClk,userRstn,netClk)
	roce1.io.s_send_data		<> XConverter(bench1.io.send_data,userClk,userRstn,netClk)
	Collector.show_more()
	Collector.connect_to_status_reg(status_reg,100)
}