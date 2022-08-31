package rps

import chisel3._
import chisel3.util._
import common.MMCME4_ADV_Wrapper
import common.IBUFDS
import cmac.CMACPin
import cmac.XCMAC
import roce.ROCE_IP
import common.ToZero
import common.storage.RegSlice
import common.storage.XQueue
import common.storage.XConverter
import common.BaseVIO
import qdma.QDMA
import qdma.QDMAPin
import common.BaseILA
import java.text.Collator
import common.Collector


class RPSClientTop extends RawModule{
	def TODO_32			= 32

	val qdma_pin		= IO(new QDMAPin())
	val cmac_pin		= IO(new CMACPin)
	val sysClkP			= IO(Input(Clock()))
	val sysClkN			= IO(Input(Clock()))
	val led		        = IO(Output(UInt(1.W)))

	led         		:= 0.U

	val mmcmTop	= Module(new MMCME4_ADV_Wrapper(
		CLKFBOUT_MULT_F 		= 12,
		MMCM_DIVCLK_DIVIDE		= 1,
		MMCM_CLKOUT0_DIVIDE_F	= 12,
		MMCM_CLKOUT1_DIVIDE_F	= 4,
		MMCM_CLKOUT2_DIVIDE_F	= 5,
		MMCM_CLKIN1_PERIOD 		= 10,
	))

	mmcmTop.io.CLKIN1	:= IBUFDS(sysClkP,sysClkN)
	mmcmTop.io.RST		:= 0.U

	val cmacClk			= mmcmTop.io.CLKOUT0 //100M
	val userClk			= mmcmTop.io.CLKOUT1 //300M
	val netClk 			= mmcmTop.io.CLKOUT2 //240M

	val cmac			= Module(new XCMAC)

	val sw_reset		= Wire(Bool())//todo
	val netRstn			= withClockAndReset(netClk,false.B) {RegNext(cmac.io.net_rstn && !sw_reset)}
	val userRstn		= withClockAndReset(userClk,false.B) {ShiftRegister(netRstn && !sw_reset,4)}

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
	qdma.io.axib.slave_init()

	qdma.io.pin <> qdma_pin
	qdma.io.user_clk	:= userClk
	qdma.io.user_arstn	:= userRstn
	qdma.io.soft_rstn	:= 1.U

	val control_reg = qdma.io.reg_control
	val status_reg = qdma.io.reg_status

	sw_reset			:= control_reg(100) === 1.U

	cmac.getTCL()
	cmac.io.pin			<> cmac_pin
	cmac.io.drp_clk		<> cmacClk
	cmac.io.user_clk	<> netClk
	cmac.io.user_arstn	<> netRstn
	cmac.io.sys_reset	<> !mmcmTop.io.LOCKED

	

	val roce:ROCE_IP						= withClockAndReset(netClk, !netRstn){Module(new ROCE_IP)}
	cmac.io.s_net_tx 						<> withClockAndReset(netClk, !netRstn){RegSlice(XQueue(roce.io.m_net_tx_data, TODO_32))}
	roce.io.s_net_rx_data					<> withClockAndReset(netClk, !netRstn){RegSlice(XQueue(cmac.io.m_net_rx, TODO_32))}
	roce.io.m_mem_read_cmd.ready			:= 1.U
	roce.io.m_mem_write_cmd.ready			:= 1.U
	roce.io.m_mem_write_data.ready			:= 1.U
	roce.io.s_mem_read_data.valid			:= 0.U
	roce.io.m_cmpt_meta.ready				:= 1.U
	ToZero(roce.io.s_mem_read_data.bits)
	ToZero(roce.io.qp_init.bits)

	roce.io.qp_init.bits.remote_udp_port	:= 17.U	
	roce.io.qp_init.bits.credit				:= 1600.U

	withClockAndReset(netClk, !netRstn){
		val start 							= RegInit(UInt(1.W),0.U)
		start								:= control_reg(101)(0)

		val risingStartInit					= RegInit(UInt(1.W),0.U)
		risingStartInit						:= start===1.U && RegNext(!start)
		val valid 							= RegInit(UInt(1.W),0.U)
		when(risingStartInit === 1.U){
			valid							:= 1.U
		}.elsewhen(roce.io.qp_init.fire()){
			valid							:= 0.U
		}
		roce.io.qp_init.valid				:= valid
		
		roce.io.qp_init.bits.remote_ip		:= 0x02bda8c0.U
		roce.io.local_ip_address			:= 0x01bda8c0.U//0x01bda8c0 01/189/168/192

		val cur_qp							= RegInit(UInt(1.W),0.U)
		when(roce.io.qp_init.fire()){
			cur_qp							:= cur_qp + 1.U
		}

		when(cur_qp === 0.U){
			roce.io.qp_init.bits.qpn			:= 1.U
			roce.io.qp_init.bits.remote_qpn		:= 1.U
			roce.io.qp_init.bits.local_psn		:= 0x1001.U
			roce.io.qp_init.bits.remote_psn		:= 0x2001.U
		}.otherwise{
			roce.io.qp_init.bits.qpn			:= 2.U
			roce.io.qp_init.bits.remote_qpn		:= 2.U
			roce.io.qp_init.bits.local_psn		:= 0x1002.U
			roce.io.qp_init.bits.remote_psn		:= 0x2002.U
		}

		// class ila_qp_init(seq:Seq[Data]) extends BaseILA(seq)
		// val inst_qp_init = Module(new ila_qp_init(Seq(	
		// 	roce.io.qp_init.valid,
		// 	roce.io.qp_init.ready,
		// 	roce.io.qp_init.bits.qpn,
		// 	roce.io.qp_init.bits.remote_qpn,
		// 	roce.io.qp_init.bits.local_psn,
		// 	roce.io.qp_init.bits.remote_psn,
		// 	roce.io.s_tx_meta,
		// 	netRstn,
		// 	control_reg(101),
		// 	risingStartInit,
		// )))
		// inst_qp_init.connect(netClk)
	}

	val clientAndCS:ClientAndChunckServer	= withClockAndReset(userClk, !userRstn){Module(new ClientAndChunckServer())}
	clientAndCS.io.recv_meta			<> XConverter(roce.io.m_recv_meta,netClk,netRstn,userClk)
	clientAndCS.io.recv_data			<> XConverter(roce.io.m_recv_data,netClk,netRstn,userClk)
	roce.io.s_tx_meta					<> XConverter(clientAndCS.io.send_meta,userClk,userRstn,netClk)
	roce.io.s_send_data					<> XConverter(clientAndCS.io.send_data,userClk,userRstn,netClk)
	withClockAndReset(userClk, !userRstn){
		val num_rpcs					= control_reg(102)
		val start						= control_reg(103) === 1.U
		clientAndCS.io.num_rpcs			:= num_rpcs
		clientAndCS.io.start			:= start
	}
	Collector.connect_to_status_reg(status_reg,100)
}