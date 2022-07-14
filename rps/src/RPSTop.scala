package rps

import chisel3._
import chisel3.util._
import common._
import common.storage.XAXIConverter
import hbm._

class RPSTop extends RawModule {
	val sysClkP			= IO(Input(Clock()))
	val sysClkN			= IO(Input(Clock()))
	val led		        = IO(Output(UInt(1.W)))

	led         		:= 0.U
	
	val ibufdsInst = Module(new IBUFDS)
	ibufdsInst.io.I		:= sysClkP
	ibufdsInst.io.IB	:= sysClkN
	val sysClk 			= ibufdsInst.io.O

	val mmcmTop	= Module(new MMCME4_ADV_Wrapper(
		CLKFBOUT_MULT_F 		= 12,
		MMCM_DIVCLK_DIVIDE		= 1,
		MMCM_CLKOUT0_DIVIDE_F	= 12,
		MMCM_CLKOUT1_DIVIDE_F	= 4,
		MMCM_CLKIN1_PERIOD 		= 10
	))

	mmcmTop.io.CLKIN1	:= sysClk
	mmcmTop.io.RST		:= 0.U

	val clk_hbm_driver	= mmcmTop.io.CLKOUT0 //100M
	val userClk		= mmcmTop.io.CLKOUT1 //300M


    val hbmDriver = withClockAndReset(clk_hbm_driver, false.B) {Module(new HBM_DRIVER(WITH_RAMA=false))}
	hbmDriver.getTCL()
	val hbmClk 	    	= hbmDriver.io.hbm_clk
	val hbmRstn     	= withClockAndReset(hbmClk,false.B) {RegNext(hbmDriver.io.hbm_rstn.asBool)}
    
	val userRstn		= withClockAndReset(userClk,false.B) {ShiftRegister(hbmRstn,4)}

	for (i <- 0 until 32) {
		hbmDriver.io.axi_hbm(i).hbm_init()	// Read hbm_init function if you're not familiar with AXI.
	}

	val hbmCompress = withClockAndReset(userClk, !userRstn) {Module(new HBMCompress)}

	for(i<-0 until 32){
		hbmDriver.io.axi_hbm(i) <> XAXIConverter(hbmCompress.io.axi(i), userClk, userRstn, hbmClk, hbmRstn)
		// hbmDriver.io.axi_hbm(i) <> hbmCompress.io.axi(i)
	}
	
    
	dontTouch(hbmClk)
	dontTouch(hbmRstn)
}