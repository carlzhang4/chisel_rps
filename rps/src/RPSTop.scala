package rps

import chisel3._
import chisel3.util._
import common._
import hbm._

class RPSTop extends RawModule {
	val sysClkP			= IO(Input(Clock()))
	val sysClkN			= IO(Input(Clock()))
	val led		        = IO(Output(UInt(1.W)))

	val sysClk = Wire(Clock())
	
	val ibufdsInst = Module(new IBUFDS)
	ibufdsInst.io.I		:= sysClkP
	ibufdsInst.io.IB	:= sysClkN
	sysClk := ibufdsInst.io.O

    val hbmDriver = withClockAndReset(sysClk, false.B) {Module(new HBM_DRIVER)}
	hbmDriver.getTCL()
	val hbmClk 	= Wire(Clock())
	val hbmRstn = Wire(UInt(1.W))

	hbmClk 	    := hbmDriver.io.hbm_clk
	hbmRstn     := hbmDriver.io.hbm_rstn
    led         := 0.U

	for (i <- 0 until 32) {
		hbmDriver.io.axi_hbm(i).hbm_init()	// Read hbm_init function if you're not familiar with AXI.
	}
    
	dontTouch(hbmClk)
	dontTouch(hbmRstn)
}