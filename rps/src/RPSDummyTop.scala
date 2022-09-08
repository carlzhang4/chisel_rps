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
import roce.ROCE_IP
import cmac.XCMAC
import cmac.CMACPin
import common.storage.RegSlice
import common.storage.XConverter
import common.storage.XQueueConfig
import common.storage.AXIRegSlice

class RPSDummyTop extends RawModule {
	val qdma_pin		= IO(new QDMAPin())
	val cmac_pin		= IO(new CMACPin)
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

	val userClk			= mmcmTop.io.CLKOUT1 //300M
	val cmacClk			= mmcmTop.io.CLKOUT2 //100M
	val netClk			= mmcmTop.io.CLKOUT3 //240M

	val cmac			= Module(new XCMAC)

	val sw_reset		= Wire(Bool())
	val netRstn			= withClockAndReset(netClk,false.B) {RegNext(cmac.io.net_rstn)}
	val userRstn		= withClockAndReset(userClk,false.B) {ShiftRegister(netRstn,4)}

	//QDMA
	val qdma 			= Module(new QDMA("202101"))
	qdma.getTCL()
	ToZero(qdma.io.reg_status)
	
	qdma.io.pin <> qdma_pin
	qdma.io.user_clk	:= userClk
	qdma.io.user_arstn	:= userRstn
	qdma.io.soft_rstn	:= 1.U

	val control_reg = qdma.io.reg_control
	val status_reg = qdma.io.reg_status

	sw_reset	:= control_reg(100) === 1.U

	
	cmac.getTCL()
	cmac.io.pin			<> cmac_pin
	cmac.io.drp_clk		<> cmacClk
	cmac.io.user_clk	<> netClk
	cmac.io.user_arstn	<> netRstn
	cmac.io.sys_reset	<> !mmcmTop.io.LOCKED

	val roce								= withClockAndReset(netClk, sw_reset || !netRstn){Module(new ROCE_IP)}

	cmac.io.s_net_tx 						<> withClockAndReset(netClk, sw_reset || !netRstn){RegSlice(XQueue(roce.io.m_net_tx_data, TODO_32))}
	roce.io.s_net_rx_data					<> withClockAndReset(netClk, sw_reset || !netRstn){RegSlice(XQueue(cmac.io.m_net_rx, TODO_32))}
	roce.io.m_mem_read_cmd.ready			:= 1.U
	roce.io.m_mem_write_cmd.ready			:= 1.U
	roce.io.m_mem_write_data.ready			:= 1.U
	roce.io.s_mem_read_data.valid			:= 0.U
	roce.io.m_cmpt_meta.ready				:= 1.U
	ToZero(roce.io.s_mem_read_data.bits)
	ToZero(roce.io.qp_init.bits)

	roce.io.qp_init.bits.remote_udp_port	:= 17.U	
		

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
		roce.io.qp_init.bits.remote_ip		:= 0x01bda8c0.U
		roce.io.local_ip_address			:= 0x02bda8c0.U//0x01bda8c0 01/189/168/192
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
	}

	val bench = withClockAndReset(userClk, sw_reset || !userRstn){Module(new BlockServer(4,12,true))}

	bench.io.start_addr			:= Cat(control_reg(110), control_reg(111))
	bench.io.num_rpcs			:= control_reg(112)
	bench.io.pfch_tag			:= control_reg(113)
	bench.io.tag_index			:= control_reg(114)
	bench.io.c2h_cmd			<> qdma.io.c2h_cmd
	bench.io.c2h_data			<> qdma.io.c2h_data
	bench.io.h2c_cmd			<> qdma.io.h2c_cmd
	bench.io.h2c_data			<> qdma.io.h2c_data
	bench.io.axib 				<> XAXIConverter(qdma.io.axib, qdma.io.pcie_clk, qdma.io.pcie_arstn, qdma.io.user_clk, qdma.io.user_arstn)
	bench.io.recv_meta			<> XConverter(roce.io.m_recv_meta,netClk,netRstn,userClk)
	bench.io.recv_data			<> XConverter(roce.io.m_recv_data,netClk,netRstn,userClk)
	roce.io.s_tx_meta			<> XConverter(bench.io.send_meta,userClk,userRstn,netClk)
	roce.io.s_send_data			<> XConverter(bench.io.send_data,userClk,userRstn,netClk)
	Collector.connect_to_status_reg(status_reg,100)
}