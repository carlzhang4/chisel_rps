package rps

import chisel3._
import chisel3.util._
import common._
import common.storage.XAXIConverter
import hbm._
import qdma.QDMAPin
import qdma.QDMA
import qdma.SimpleAXISlave
import qdma.AXIB
import qdma.H2C
import qdma.H2CLatency
import qdma.C2HLatency

class RPSTop extends RawModule {
	val qdma_pin		= IO(new QDMAPin())
	val sysClkP			= IO(Input(Clock()))
	val sysClkN			= IO(Input(Clock()))
	val led		        = IO(Output(UInt(1.W)))

	led         		:= 0.U
	
	val mmcmTop	= Module(new MMCME4_ADV_Wrapper(
		CLKFBOUT_MULT_F 		= 12,
		MMCM_DIVCLK_DIVIDE		= 1,
		MMCM_CLKOUT0_DIVIDE_F	= 12,
		MMCM_CLKOUT1_DIVIDE_F	= 4,
		MMCM_CLKIN1_PERIOD 		= 10
	))

	mmcmTop.io.CLKIN1	:= IBUFDS(sysClkP,sysClkN)
	mmcmTop.io.RST		:= 0.U

	val clk_hbm_driver	= mmcmTop.io.CLKOUT0 //100M
	val userClk			= mmcmTop.io.CLKOUT1 //300M

	//HBM
    val hbmDriver 		= withClockAndReset(clk_hbm_driver, false.B) {Module(new HBM_DRIVER(WITH_RAMA=false))}
	hbmDriver.getTCL()
	val hbmClk 	    	= hbmDriver.io.hbm_clk
	val hbmRstn     	= withClockAndReset(hbmClk,false.B) {RegNext(hbmDriver.io.hbm_rstn.asBool)}
    
	val userRstn		= withClockAndReset(userClk,false.B) {ShiftRegister(hbmRstn,4)}

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
	qdma.io.soft_rstn	:= 1.U


	val control_reg = qdma.io.reg_control
	val status_reg = qdma.io.reg_status

	val sw_reset	= control_reg(106) === 1.U

	val bench = withClockAndReset(userClk, sw_reset || !userRstn){Module(new BenchNetSim(4,12))}

	hbmDriver.io.axi_hbm(0) <> XAXIConverter(bench.io.axi_hbm(0), userClk, userRstn, hbmClk, hbmRstn)
	hbmDriver.io.axi_hbm(1) <> XAXIConverter(bench.io.axi_hbm(1), userClk, userRstn, hbmClk, hbmRstn)
	hbmDriver.io.axi_hbm(2) <> XAXIConverter(bench.io.axi_hbm(2), userClk, userRstn, hbmClk, hbmRstn)
	hbmDriver.io.axi_hbm(3) <> XAXIConverter(bench.io.axi_hbm(3), userClk, userRstn, hbmClk, hbmRstn)

	bench.io.start_addr			:= Cat(control_reg(100), control_reg(101))
	bench.io.num_rpcs			:= control_reg(102)
	bench.io.pfch_tag			:= control_reg(103)
	bench.io.tag_index			:= control_reg(104)
	bench.io.start				:= control_reg(105)
	bench.io.c2h_cmd			<> qdma.io.c2h_cmd
	bench.io.c2h_data			<> qdma.io.c2h_data
	bench.io.h2c_cmd			<> qdma.io.h2c_cmd
	bench.io.h2c_data			<> qdma.io.h2c_data
	bench.io.axib 				<> XAXIConverter(qdma.io.axib, qdma.io.pcie_clk, qdma.io.pcie_arstn, qdma.io.user_clk, qdma.io.user_arstn)

	for(i<-0 until RPSConters.MAX_NUM){
		status_reg(100+i) 		:= bench.io.counters(i)
	}
}