package rps
import chisel3._
import chisel3.util._
import qdma.QDMAPin
import common.MMCME4_ADV_Wrapper
import common.IBUFDS
import qdma.QDMA
import common.ToZero
import common.storage.XAXIConverter
import common.Collector

class RPSCompressTop extends RawModule{
	val qdma_pin		= IO(new QDMAPin())
	val sysClkP			= IO(Input(Clock()))
	val sysClkN			= IO(Input(Clock()))
	val led		        = IO(Output(UInt(1.W)))

	led         		:= 0.U

	val mmcmTop	= Module(new MMCME4_ADV_Wrapper(
		CLKFBOUT_MULT_F 		= 12,
		MMCM_DIVCLK_DIVIDE		= 1,
		MMCM_CLKOUT0_DIVIDE_F	= 4,
		MMCM_CLKIN1_PERIOD 		= 10,
	))
	mmcmTop.io.CLKIN1	:= IBUFDS(sysClkP,sysClkN)
	mmcmTop.io.RST		:= 0.U

	val userClk			= mmcmTop.io.CLKOUT0 //300M
	val sw_reset		= Wire(Bool())

	val userRstn		= withClockAndReset(userClk,false.B) {ShiftRegister(mmcmTop.io.LOCKED.asBool(),4)}

	val qdma 			= Module(new QDMA("202101"))
	qdma.getTCL()
	ToZero(qdma.io.reg_status)
	
	qdma.io.pin 		<> qdma_pin
	qdma.io.user_clk	:= userClk
	qdma.io.user_arstn	:= userRstn

	val control_reg = qdma.io.reg_control
	val status_reg = qdma.io.reg_status
	
	sw_reset	:= control_reg(100) === 1.U

	val bench = withClockAndReset(userClk, sw_reset || !userRstn){Module(new QATBench())}
	bench.io.start_addr			:= Cat(control_reg(110), control_reg(111))
	bench.io.pfch_tag			:= control_reg(113)
	bench.io.tag_index			:= control_reg(114)
	bench.io.c2h_cmd			<> qdma.io.c2h_cmd
	bench.io.c2h_data			<> qdma.io.c2h_data
	bench.io.h2c_cmd			<> qdma.io.h2c_cmd
	bench.io.h2c_data			<> qdma.io.h2c_data
	bench.io.axib 				<> XAXIConverter(qdma.io.axib, qdma.io.pcie_clk, qdma.io.pcie_arstn, qdma.io.user_clk, qdma.io.user_arstn)

	Collector.connect_to_status_reg(status_reg,100)
}