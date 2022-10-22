package rps

import chisel3._
import chisel3.util._
import common.MMCME4_ADV_Wrapper
import common.IBUFDS
import network.cmac.CMACPin
import network.cmac.XCMAC
import network.NetworkStack
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
import chisel3.util.experimental.BoringUtils


class RPSClientTop extends RawModule{
	def TODO_32			= 32

	val qdma_pin		= IO(new QDMAPin())
	val cmac_pin		= IO(new CMACPin)
	val cmac_pin1		= IO(new CMACPin)
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

	val sw_reset		= Wire(Bool())//todo
	val netRstn			= withClockAndReset(netClk,false.B) {RegNext(mmcmTop.io.LOCKED.asBool())}
	val userRstn		= withClockAndReset(userClk,false.B) {ShiftRegister(mmcmTop.io.LOCKED.asBool(),4)}
	//do not add sw_reset into Rstn, otherwise sw_reset after QDMA initiation would reset QDMA's TLB

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

	val control_reg = qdma.io.reg_control
	val status_reg = qdma.io.reg_status

	sw_reset			:= control_reg(100) === 1.U
	
	val roce								= Module(new NetworkStack)
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
	val roce1								= Module(new NetworkStack(IS_PART_1=true))
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
		
		roce.io.qp_init.bits.remote_ip		:= 0x52bda8c0.U
		roce.io.ip_address					:= 0x51bda8c0.U//0x01bda8c0 01/189/168/192
		roce.io.qp_init.bits.credit			:= control_reg(103)

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

		val valid_arp						= RegInit(UInt(1.W),0.U)
		when(risingStartInit === 1.U){
			valid_arp						:= 1.U
		}.elsewhen(roce.io.arp_req.fire()){
			valid_arp						:= 0.U
		}
		roce.io.arp_req.valid				:= valid_arp
		roce.io.arp_req.bits				:= 0x52bda8c0.U
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
		roce1.io.qp_init.bits.remote_ip		:= 0x62bda8c0.U
		roce1.io.ip_address					:= 0x61bda8c0.U//0x01bda8c0 01/189/168/192
		roce1.io.qp_init.bits.credit		:= control_reg(103)

		val cur_qp							= RegInit(UInt(1.W),0.U)
		when(roce1.io.qp_init.fire()){
			cur_qp							:= cur_qp + 1.U
		}

		when(cur_qp === 0.U){
			roce1.io.qp_init.bits.qpn			:= 1.U
			roce1.io.qp_init.bits.remote_qpn	:= 1.U
			roce1.io.qp_init.bits.local_psn		:= 0x3001.U
			roce1.io.qp_init.bits.remote_psn	:= 0x4001.U
		}.otherwise{
			roce1.io.qp_init.bits.qpn			:= 2.U
			roce1.io.qp_init.bits.remote_qpn	:= 2.U
			roce1.io.qp_init.bits.local_psn		:= 0x3002.U
			roce1.io.qp_init.bits.remote_psn	:= 0x4002.U
		}

		val valid_arp						= RegInit(UInt(1.W),0.U)
		when(risingStartInit === 1.U){
			valid_arp						:= 1.U
		}.elsewhen(roce1.io.arp_req.fire()){
			valid_arp						:= 0.U
		}
		roce1.io.arp_req.valid				:= valid_arp
		roce1.io.arp_req.bits				:= 0x62bda8c0.U
	}

	val clientAndCS:ClientAndChunckServer	= withClockAndReset(userClk, sw_reset || !userRstn){Module(new ClientAndChunckServer())}
	clientAndCS.io.recv_meta			<> XConverter(roce.io.m_recv_meta,netClk,netRstn & !sw_reset,userClk)
	clientAndCS.io.recv_data			<> XConverter(roce.io.m_recv_data,netClk,netRstn & !sw_reset,userClk)
	roce.io.s_tx_meta					<> XConverter(clientAndCS.io.send_meta,userClk,userRstn & !sw_reset,netClk)
	roce.io.s_send_data					<> XConverter(clientAndCS.io.send_data,userClk,userRstn & !sw_reset,netClk)
	withClockAndReset(userClk, !userRstn){
		val start						= RegNext(control_reg(102) === 1.U)
		clientAndCS.io.num_rpcs			:= control_reg(110)
		clientAndCS.io.en_cycles		:= control_reg(111)
		clientAndCS.io.total_cycles		:= control_reg(112)
		clientAndCS.io.start			:= start
	}

	val clientAndCS1:ClientAndChunckServer	= withClockAndReset(userClk, sw_reset || !userRstn){Module(new ClientAndChunckServer())}
	clientAndCS1.io.recv_meta			<> XConverter(roce1.io.m_recv_meta,netClk,netRstn & !sw_reset,userClk)
	clientAndCS1.io.recv_data			<> XConverter(roce1.io.m_recv_data,netClk,netRstn & !sw_reset,userClk)
	roce1.io.s_tx_meta					<> XConverter(clientAndCS1.io.send_meta,userClk,userRstn & !sw_reset,netClk)
	roce1.io.s_send_data				<> XConverter(clientAndCS1.io.send_data,userClk,userRstn & !sw_reset,netClk)
	withClockAndReset(userClk, !userRstn){
		val start						= RegNext(control_reg(102) === 1.U)
		clientAndCS1.io.num_rpcs		:= control_reg(110)
		clientAndCS1.io.en_cycles		:= control_reg(111)
		clientAndCS1.io.total_cycles	:= control_reg(112)
		clientAndCS1.io.start			:= start
	}
	BoringUtils.addSource(control_reg(113),"global_client_maxCredit")
	Collector.show_more()
	Collector.connect_to_status_reg(status_reg,100)
}