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
	var ChannelOffset	= Array(0,0,0,0)
}
class RPSTop extends RawModule {
	val qdma_pin		= IO(new QDMAPin())
	val cmac_pin		= IO(new CMACPin)
	val cmac_pin1		= IO(new CMACPin)
	val cmac_pin2		= IO(new CMACPin)
	val cmac_pin3		= IO(new CMACPin)
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
	val roce1								= Module(new NetworkStack(PART_ID=1))
	val roce2								= Module(new NetworkStack(PART_ID=2))
	val roce3								= Module(new NetworkStack(PART_ID=3))

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

	roce2.io.pin							<> cmac_pin2
	roce2.io.user_clk						:= netClk
	roce2.io.user_arstn						:= netRstn
	roce2.io.sys_reset						:= !netRstn
	roce2.io.drp_clk						:= cmacClk
	roce2.io.m_mem_read_cmd.ready			:= 1.U
	roce2.io.m_mem_write_cmd.ready			:= 1.U
	roce2.io.m_mem_write_data.ready			:= 1.U
	roce2.io.s_mem_read_data.valid			:= 0.U
	roce2.io.m_cmpt_meta.ready				:= 1.U
	roce2.io.arp_rsp.ready					:= 1.U
	roce2.io.arp_req.valid					:= 0.U
	roce2.io.sw_reset						:= sw_reset
	ToZero(roce2.io.s_mem_read_data.bits)
	ToZero(roce2.io.qp_init.bits)

	roce3.io.pin							<> cmac_pin3
	roce3.io.user_clk						:= netClk
	roce3.io.user_arstn						:= netRstn
	roce3.io.sys_reset						:= !netRstn
	roce3.io.drp_clk						:= cmacClk
	roce3.io.m_mem_read_cmd.ready			:= 1.U
	roce3.io.m_mem_write_cmd.ready			:= 1.U
	roce3.io.m_mem_write_data.ready			:= 1.U
	roce3.io.s_mem_read_data.valid			:= 0.U
	roce3.io.m_cmpt_meta.ready				:= 1.U
	roce3.io.arp_rsp.ready					:= 1.U
	roce3.io.arp_req.valid					:= 0.U
	roce3.io.sw_reset						:= sw_reset
	ToZero(roce3.io.s_mem_read_data.bits)
	ToZero(roce3.io.qp_init.bits)



	roce.io.qp_init.bits.remote_udp_port	:= 17.U	
	roce1.io.qp_init.bits.remote_udp_port	:= 17.U	
	roce2.io.qp_init.bits.remote_udp_port	:= 17.U	
	roce3.io.qp_init.bits.remote_udp_port	:= 17.U			

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

	withClockAndReset(netClk, sw_reset || !netRstn){
		val start 							= RegNext(control_reg(101) === 1.U)
		val risingStartInit					= start && RegNext(!start)
		val valid 							= RegInit(UInt(1.W),0.U)
		when(risingStartInit === 1.U){
			valid							:= 1.U
		}.elsewhen(roce2.io.qp_init.fire()){
			valid							:= 0.U
		}
		roce2.io.qp_init.valid				:= valid
		roce2.io.qp_init.bits.remote_ip		:= 0x51bda8c0.U
		roce2.io.ip_address					:= 0x52bda8c0.U//0x01bda8c0 01/189/168/192
		roce2.io.qp_init.bits.credit			:= control_reg(103)

		val cur_qp							= RegInit(UInt(1.W),0.U)
		when(roce.io.qp_init.fire()){
			cur_qp							:= cur_qp + 1.U
		}

		when(cur_qp === 0.U){
			roce2.io.qp_init.bits.qpn			:= 1.U
			roce2.io.qp_init.bits.remote_qpn		:= 1.U
			roce2.io.qp_init.bits.local_psn		:= 0x2001.U
			roce2.io.qp_init.bits.remote_psn		:= 0x1001.U
		}.otherwise{
			roce2.io.qp_init.bits.qpn			:= 2.U
			roce2.io.qp_init.bits.remote_qpn		:= 2.U
			roce2.io.qp_init.bits.local_psn		:= 0x2002.U
			roce2.io.qp_init.bits.remote_psn		:= 0x1002.U
		}

		val valid_arp						= RegInit(UInt(1.W),0.U)
		when(risingStartInit === 1.U){
			valid_arp						:= 1.U
		}.elsewhen(roce2.io.arp_req.fire()){
			valid_arp						:= 0.U
		}
		roce2.io.arp_req.valid				:= valid_arp
		roce2.io.arp_req.bits				:= 0x51bda8c0.U
	}

	withClockAndReset(netClk, sw_reset || !netRstn){
		val start 							= RegNext(control_reg(101) === 1.U)
		val risingStartInit					= start && RegNext(!start)
		val valid 							= RegInit(UInt(1.W),0.U)
		when(risingStartInit === 1.U){
			valid							:= 1.U
		}.elsewhen(roce3.io.qp_init.fire()){
			valid							:= 0.U
		}
		roce3.io.qp_init.valid				:= valid
		roce3.io.qp_init.bits.remote_ip		:= 0x61bda8c0.U
		roce3.io.ip_address					:= 0x62bda8c0.U//0x01bda8c0 01/189/168/192
		roce3.io.qp_init.bits.credit		:= control_reg(103)

		val cur_qp							= RegInit(UInt(1.W),0.U)
		when(roce3.io.qp_init.fire()){
			cur_qp							:= cur_qp + 1.U
		}

		when(cur_qp === 0.U){
			roce3.io.qp_init.bits.qpn			:= 1.U
			roce3.io.qp_init.bits.remote_qpn	:= 1.U
			roce3.io.qp_init.bits.local_psn		:= 0x4001.U
			roce3.io.qp_init.bits.remote_psn	:= 0x3001.U
		}.otherwise{
			roce3.io.qp_init.bits.qpn			:= 2.U
			roce3.io.qp_init.bits.remote_qpn	:= 2.U
			roce3.io.qp_init.bits.local_psn		:= 0x4002.U
			roce3.io.qp_init.bits.remote_psn	:= 0x3002.U
		}

		val valid_arp						= RegInit(UInt(1.W),0.U)
		when(risingStartInit === 1.U){
			valid_arp						:= 1.U
		}.elsewhen(roce3.io.arp_req.fire()){
			valid_arp						:= 0.U
		}
		roce3.io.arp_req.valid				:= valid_arp
		roce3.io.arp_req.bits				:= 0x61bda8c0.U
	}

	
	GlobalConfig.ChannelOffset(0)		= 8
	GlobalConfig.ChannelOffset(1)		= 12
	GlobalConfig.ChannelOffset(2)		= 16
	GlobalConfig.ChannelOffset(3)		= 20

	val bench	= withClockAndReset(userClk, sw_reset || !userRstn){Module(new BlockServer(4,12,false,0))}
	val bench1	= withClockAndReset(userClk, sw_reset || !userRstn){Module(new BlockServer(4,12,false,1))}
	val bench2	= withClockAndReset(userClk, sw_reset || !userRstn){Module(new BlockServer(4,12,false,2))}
	val bench3	= withClockAndReset(userClk, sw_reset || !userRstn){Module(new BlockServer(4,12,false,3))}

	withClockAndReset(userClk,!userRstn){
		val arbiter	= CompositeArbiter(new C2H_CMD,new C2H_DATA,4)
		arbiter.io.out_meta	<> qdma.io.c2h_cmd
		arbiter.io.out_data	<> qdma.io.c2h_data

		arbiter.io.in_meta(0)	<> bench.io.c2h_cmd
		arbiter.io.in_data(0)	<> bench.io.c2h_data
		arbiter.io.in_meta(1)	<> bench1.io.c2h_cmd
		arbiter.io.in_data(1)	<> bench1.io.c2h_data
		arbiter.io.in_meta(2)	<> bench2.io.c2h_cmd
		arbiter.io.in_data(2)	<> bench2.io.c2h_data
		arbiter.io.in_meta(3)	<> bench3.io.c2h_cmd
		arbiter.io.in_data(3)	<> bench3.io.c2h_data

		// class ila_c2h(seq:Seq[Data]) extends BaseILA(seq)
		// val mod_c2h = Module(new ila_c2h(Seq(	
		// 	qdma.io.c2h_cmd.valid,
		// 	qdma.io.c2h_cmd.ready,
		// 	qdma.io.c2h_cmd.bits.addr,
		// 	qdma.io.c2h_data.valid,
		// 	qdma.io.c2h_data.ready,
		// 	qdma.io.c2h_data.bits.data,
		// )))
		// mod_c2h.connect(userClk)


		val router				= SimpleRouter(chiselTypeOf(qdma.io.axib.w.bits),4)
		router.io.in			<> XConverter(qdma.io.axib.w,qdma.io.pcie_clk,qdma.io.pcie_arstn,userClk)
		when(router.io.in.bits.data(479,448) === 0.U){
			router.io.idx		:= 0.U
		}.elsewhen(router.io.in.bits.data(479,448) === 1.U){
			router.io.idx		:= 1.U
		}.elsewhen(router.io.in.bits.data(479,448) === 2.U){
			router.io.idx		:= 2.U
		}.elsewhen(router.io.in.bits.data(479,448) === 3.U){
			router.io.idx		:= 3.U
		}.otherwise{
			router.io.idx		:= 0.U
		}
		router.io.out(0)		<> bench.io.axib.w
		router.io.out(1)		<> bench1.io.axib.w
		router.io.out(2)		<> bench2.io.axib.w
		router.io.out(3)		<> bench3.io.axib.w

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

		Init(bench2.io.axib.b)
		Init(bench2.io.axib.ar)
		Init(bench2.io.axib.r)
		Init(bench2.io.axib.aw)

		Init(bench3.io.axib.b)
		Init(bench3.io.axib.ar)
		Init(bench3.io.axib.r)
		Init(bench3.io.axib.aw)		

	}

	hbmDriver.io.axi_hbm(8) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench.io.axi_hbm.get(0), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(9) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench.io.axi_hbm.get(1), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(10) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench.io.axi_hbm.get(2), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(11) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench.io.axi_hbm.get(3), userClk, userRstn, hbmClk, hbmRstn))}

	hbmDriver.io.axi_hbm(12) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench1.io.axi_hbm.get(0), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(13) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench1.io.axi_hbm.get(1), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(14) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench1.io.axi_hbm.get(2), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(15) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench1.io.axi_hbm.get(3), userClk, userRstn, hbmClk, hbmRstn))}

	hbmDriver.io.axi_hbm(16) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench2.io.axi_hbm.get(0), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(17) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench2.io.axi_hbm.get(1), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(18) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench2.io.axi_hbm.get(2), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(19) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench2.io.axi_hbm.get(3), userClk, userRstn, hbmClk, hbmRstn))}

	hbmDriver.io.axi_hbm(20) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench3.io.axi_hbm.get(0), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(21) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench3.io.axi_hbm.get(1), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(22) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench3.io.axi_hbm.get(2), userClk, userRstn, hbmClk, hbmRstn))}
	hbmDriver.io.axi_hbm(23) <> withClockAndReset(hbmClk,!hbmRstn){ AXIRegSlice(2)(XAXIConverter(bench3.io.axi_hbm.get(3), userClk, userRstn, hbmClk, hbmRstn))}

	bench.io.start_addr			:= Cat(control_reg(110), control_reg(111))
	bench.io.pfch_tag			:= control_reg(113)
	bench.io.tag_index			:= control_reg(114)
	bench.io.h2c_cmd			<> qdma.io.h2c_cmd
	bench.io.h2c_data			<> qdma.io.h2c_data

	bench.io.recv_meta			<> XConverter(roce.io.m_recv_meta,netClk,netRstn,userClk)
	bench.io.recv_data			<> XConverter(roce.io.m_recv_data,netClk,netRstn,userClk)
	roce.io.s_tx_meta			<> XConverter(bench.io.send_meta,userClk,userRstn,netClk)
	roce.io.s_send_data			<> XConverter(bench.io.send_data,userClk,userRstn,netClk)

	bench1.io.start_addr		:= Cat(control_reg(115), control_reg(116))
	bench1.io.pfch_tag			:= control_reg(113)
	bench1.io.tag_index			:= control_reg(114)
	bench1.io.h2c_cmd.ready		:= 0.U
	bench1.io.h2c_data.valid	:= 0.U
	ToZero(bench1.io.h2c_data.bits)

	bench1.io.recv_meta			<> XConverter(roce1.io.m_recv_meta,netClk,netRstn,userClk)
	bench1.io.recv_data			<> XConverter(roce1.io.m_recv_data,netClk,netRstn,userClk)
	roce1.io.s_tx_meta			<> XConverter(bench1.io.send_meta,userClk,userRstn,netClk)
	roce1.io.s_send_data		<> XConverter(bench1.io.send_data,userClk,userRstn,netClk)

	bench2.io.start_addr			:= Cat(control_reg(117), control_reg(118))
	bench2.io.pfch_tag			:= control_reg(113)
	bench2.io.tag_index			:= control_reg(114)
	bench2.io.h2c_cmd			<> qdma.io.h2c_cmd
	bench2.io.h2c_data			<> qdma.io.h2c_data

	bench2.io.recv_meta			<> XConverter(roce2.io.m_recv_meta,netClk,netRstn,userClk)
	bench2.io.recv_data			<> XConverter(roce2.io.m_recv_data,netClk,netRstn,userClk)
	roce2.io.s_tx_meta			<> XConverter(bench2.io.send_meta,userClk,userRstn,netClk)
	roce2.io.s_send_data			<> XConverter(bench2.io.send_data,userClk,userRstn,netClk)

	bench3.io.start_addr		:= Cat(control_reg(119), control_reg(120))
	bench3.io.pfch_tag			:= control_reg(113)
	bench3.io.tag_index			:= control_reg(114)
	bench3.io.h2c_cmd.ready		:= 0.U
	bench3.io.h2c_data.valid	:= 0.U
	ToZero(bench3.io.h2c_data.bits)

	bench3.io.recv_meta			<> XConverter(roce3.io.m_recv_meta,netClk,netRstn,userClk)
	bench3.io.recv_data			<> XConverter(roce3.io.m_recv_data,netClk,netRstn,userClk)
	roce3.io.s_tx_meta			<> XConverter(bench3.io.send_meta,userClk,userRstn,netClk)
	roce3.io.s_send_data		<> XConverter(bench3.io.send_data,userClk,userRstn,netClk)



	Collector.show_more()
	Collector.connect_to_status_reg(status_reg,100)
}