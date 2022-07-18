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

	val hbmCompress = withClockAndReset(userClk, !userRstn) {Module(new HBMCompress(4,12))}
	hbmDriver.io.axi_hbm(0) <> XAXIConverter(hbmCompress.io.axi(0), userClk, userRstn, hbmClk, hbmRstn)
	hbmDriver.io.axi_hbm(1) <> XAXIConverter(hbmCompress.io.axi(1), userClk, userRstn, hbmClk, hbmRstn)
	hbmDriver.io.axi_hbm(2) <> XAXIConverter(hbmCompress.io.axi(2), userClk, userRstn, hbmClk, hbmRstn)
	hbmDriver.io.axi_hbm(3) <> XAXIConverter(hbmCompress.io.axi(3), userClk, userRstn, hbmClk, hbmRstn)
	
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

	val axi_slave = withClockAndReset(qdma.io.user_clk,!qdma.io.user_arstn){Module(new SimpleAXISlave(new AXIB))}//withClockAndReset(qdma.io.pcie_clk,!qdma.io.pcie_arstn)
	axi_slave.io.axi	<> XAXIConverter(qdma.io.axib, qdma.io.pcie_clk, qdma.io.pcie_arstn, qdma.io.user_clk, qdma.io.user_arstn)

	val control_reg = qdma.io.reg_control
	val status_reg = qdma.io.reg_status

	//H2C
	val h2c =  withClockAndReset(qdma.io.user_clk,!qdma.io.user_arstn){Module(new H2CLatency())}
	h2c.io.start_addr			:= Cat(control_reg(100), control_reg(101))
	h2c.io.burst_length			:= control_reg(102)
	h2c.io.start				:= control_reg(103)
	h2c.io.total_words			:= control_reg(104)
	h2c.io.total_cmds			:= control_reg(105)
	h2c.io.wait_cycles			:= control_reg(106)
	
	h2c.io.count_err_data		<> status_reg(100)
	h2c.io.count_right_data		<> status_reg(101)
	h2c.io.count_total_words	<> status_reg(102)
	h2c.io.count_send_cmd		<> status_reg(103)
	h2c.io.count_time			<> status_reg(104)
	status_reg(105)				<> h2c.io.count_latency(31,0)
	status_reg(106)				<> h2c.io.count_latency(63,32)
	h2c.io.h2c_cmd				<> qdma.io.h2c_cmd
	h2c.io.h2c_data				<> qdma.io.h2c_data

	//C2H
	val c2h = withClockAndReset(qdma.io.user_clk,!qdma.io.user_arstn){Module(new C2HLatency())}
	c2h.io.start_addr			:= Cat(control_reg(200), control_reg(201))
	c2h.io.burst_length			:= control_reg(202)
	c2h.io.offset				:= control_reg(203)
	c2h.io.start				:= control_reg(204)
	c2h.io.total_words			:= control_reg(205)
	c2h.io.total_cmds			:= control_reg(206)
	c2h.io.wait_cycles			:= control_reg(207)
	c2h.io.pfch_tag				:= control_reg(209)
	c2h.io.tag_index			:= control_reg(210)
	c2h.io.ack_fire				:= axi_slave.io.axi.w.fire()
	
	c2h.io.count_send_cmd		<> status_reg(200)
	c2h.io.count_send_word		<> status_reg(201)
	c2h.io.count_time			<> status_reg(202)
	status_reg(203)				<> c2h.io.count_latency_cmd(31,0)
	status_reg(204)				<> c2h.io.count_latency_cmd(63,32)
	status_reg(205)				<> c2h.io.count_latency_data(31,0)
	status_reg(206)				<> c2h.io.count_latency_data(63,32)
	c2h.io.count_recv_ack		<> status_reg(207)
	c2h.io.c2h_cmd			<> qdma.io.c2h_cmd
	c2h.io.c2h_data			<> qdma.io.c2h_data
}