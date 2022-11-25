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

class RPSTest extends RawModule {
	val cmac_pin		= IO(new CMACPin)
	val cmac_pin1		= IO(new CMACPin)
	val cmac_pin2		= IO(new CMACPin)
	val cmac_pin3		= IO(new CMACPin)
	val sysClkP			= IO(Input(Clock()))
	val sysClkN			= IO(Input(Clock()))

	
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
	val user_rstn 		= mmcmTop.io.LOCKED



	val cmac = Module(new XCMAC(PART_ID=0))
	val cmac1 = Module(new XCMAC(PART_ID=1))
	val cmac2 = Module(new XCMAC(PART_ID=2))
	val cmac3 = Module(new XCMAC(PART_ID=3))

	cmac.io.pin				<> cmac_pin
	cmac.io.drp_clk         := cmacClk
	cmac.io.user_clk	    := userClk
	cmac.io.user_arstn	    := user_rstn
	cmac.io.sys_reset 		:= !user_rstn

	cmac1.io.pin				<> cmac_pin1
	cmac1.io.drp_clk         := cmacClk
	cmac1.io.user_clk	    := userClk
	cmac1.io.user_arstn	    := user_rstn
	cmac1.io.sys_reset 		:= !user_rstn

	cmac2.io.pin				<> cmac_pin2
	cmac2.io.drp_clk         := cmacClk
	cmac2.io.user_clk	    := userClk
	cmac2.io.user_arstn	    := user_rstn
	cmac2.io.sys_reset 		:= !user_rstn

	cmac3.io.pin				<> cmac_pin3
	cmac3.io.drp_clk         := cmacClk
	cmac3.io.user_clk	    := userClk
	cmac3.io.user_arstn	    := user_rstn
	cmac3.io.sys_reset 		:= !user_rstn		


	cmac.io.m_net_rx		<> cmac.io.s_net_tx
	cmac1.io.m_net_rx		<> cmac1.io.s_net_tx

//cmac2

	class ila_tx0(seq:Seq[Data]) extends BaseILA(seq)	  
  	val tx0 = Module(new ila_tx0(Seq(	
		cmac.io.s_net_tx.valid,
	  	cmac.io.s_net_tx.ready,
  	)))
  	tx0.connect(cmac.io.net_clk)

	class ila_tx1(seq:Seq[Data]) extends BaseILA(seq)	  
  	val tx1 = Module(new ila_tx1(Seq(	
		cmac2.io.s_net_tx.valid,
	  	cmac2.io.s_net_tx.ready,
  	)))
  	tx1.connect(cmac2.io.net_clk)

	class ila_tx00(seq:Seq[Data]) extends BaseILA(seq)	  
  	val tx00 = Module(new ila_tx00(Seq(	
		cmac.io.s_net_tx.valid,
	  	cmac.io.s_net_tx.ready,
  	)))
  	tx00.connect(userClk)


	cmac2.io.m_net_rx.ready := 1.U

	val rx_data2 = Wire(UInt(32.W))
	rx_data2			:= cmac2.io.m_net_rx.bits.data(31,0)
  	class ila_rx2(seq:Seq[Data]) extends BaseILA(seq)
  	val mod_rx2 = Module(new ila_rx2(Seq(	
		cmac2.io.m_net_rx.valid,
	  	cmac2.io.m_net_rx.ready,
    	rx_data2,
    	cmac2.io.m_net_rx.bits.last
  	)))
  	mod_rx2.connect(userClk)


	class ila_tx2(seq:Seq[Data]) extends BaseILA(seq)	  
  	val tx2 = Module(new ila_tx2(Seq(	
		cmac2.io.s_net_tx.valid,
	  	cmac2.io.s_net_tx.ready,
    	cmac2.io.s_net_tx.bits.last
  	)))
  	tx2.connect(userClk)

  	val send2 = Wire(Bool())
	
	class vio_net2(seq:Seq[Data]) extends BaseVIO(seq)
  	val mod_vio2 = Module(new vio_net2(Seq(
    	send2
  	)))
  	mod_vio2.connect(userClk)

	withClockAndReset(userClk,!user_rstn){

		val data_cnt2 = RegInit(0.U(16.W))
		val tx_valid2 = RegInit(0.U(1.W))
		when(cmac2.io.s_net_tx.fire()){
			when(data_cnt2 === 20.U){
				data_cnt2	:= 0.U
			}.otherwise{
				data_cnt2	:= data_cnt2 + 1.U;
			}
		}

		when((!RegNext(send2))&send2){
			tx_valid2	:= 1.U
		}.elsewhen(data_cnt2 === 20.U){
			tx_valid2	:= 0.U
		}.otherwise{
			tx_valid2	:= tx_valid2
		}	
		cmac2.io.s_net_tx.valid 	:= tx_valid2
		cmac2.io.s_net_tx.bits.data		:= data_cnt2
		cmac2.io.s_net_tx.bits.keep		:= "hffffffffffffffff".U
		cmac2.io.s_net_tx.bits.last		:= cmac2.io.s_net_tx.fire() & (data_cnt2 === 20.U)
	}	



//cmac3

	cmac3.io.m_net_rx.ready := 1.U

	val rx_data3 = Wire(UInt(32.W))
	rx_data3			:= cmac3.io.m_net_rx.bits.data(31,0)
  	class ila_rx(seq:Seq[Data]) extends BaseILA(seq)
  	val mod_rx = Module(new ila_rx(Seq(	
		cmac3.io.m_net_rx.valid,
	  	cmac3.io.m_net_rx.ready,
    	rx_data3,
    	cmac3.io.m_net_rx.bits.last
  	)))
  	mod_rx.connect(userClk)


	class ila_tx(seq:Seq[Data]) extends BaseILA(seq)	  
  	val tx = Module(new ila_tx(Seq(	
		cmac3.io.s_net_tx.valid,
	  	cmac3.io.s_net_tx.ready,
    	cmac3.io.s_net_tx.bits.last
  	)))
  	tx.connect(userClk)

  	val send = Wire(Bool())
	
	class vio_net(seq:Seq[Data]) extends BaseVIO(seq)
  	val mod_vio = Module(new vio_net(Seq(
    	send
  	)))
  	mod_vio.connect(userClk)

	withClockAndReset(userClk,!user_rstn){

		val data_cnt = RegInit(0.U(16.W))
		val tx_valid = RegInit(0.U(1.W))
		when(cmac3.io.s_net_tx.fire()){
			when(data_cnt === 20.U){
				data_cnt	:= 0.U
			}.otherwise{
				data_cnt	:= data_cnt + 1.U;
			}
		}

		when((!RegNext(send))&send){
			tx_valid	:= 1.U
		}.elsewhen(data_cnt === 20.U){
			tx_valid	:= 0.U
		}.otherwise{
			tx_valid	:= tx_valid
		}	
		cmac3.io.s_net_tx.valid 	:= tx_valid
		cmac3.io.s_net_tx.bits.data		:= data_cnt
		cmac3.io.s_net_tx.bits.keep		:= "hffffffffffffffff".U
		cmac3.io.s_net_tx.bits.last		:= cmac3.io.s_net_tx.fire() & (data_cnt === 20.U)
	}	












}