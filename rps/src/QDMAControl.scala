package rps

import chisel3._
import chisel3.util._
import qdma.H2C_CMD
import qdma.H2C_DATA
import qdma.C2H_CMD
import qdma.C2H_DATA
import qdma.AXIB
import common.ToZero
import common.storage.XQueue
import common.connection.SimpleRouter
import common.connection.Connection
import common.Collector
import common.Timer
import common.Statistics
import common.axi.AXIS
import common.BaseILA

class QDMAControl extends Module{
	val NUM_Q = 4
	def TODO_512 = 512
	def TODO_64 = 64
	def NUM_AXIB_TYPE = 4
	val NUM_AXIB_TYPE_BITS = log2Up(NUM_AXIB_TYPE)

	val io = IO(new Bundle{
		val pfch_tag				= Input(UInt(32.W))
		val tag_index				= Input(UInt(32.W))
		val start_addr				= Input(UInt(64.W))

		val writeCMD				= Flipped(Decoupled(new WriteCMD))
		val writeData				= Flipped(Decoupled(new WriteData))
		val readCMD					= Flipped(Decoupled(new ReadCMD))
		val readData				= Decoupled(new ReadData)

		val axib_data				= Decoupled(new AxiBridgeData)

		val axi						= Flipped(new AXIB)
		val c2h_cmd					= Decoupled(new C2H_CMD)
		val c2h_data				= Decoupled(new C2H_DATA) 
		val h2c_cmd					= Decoupled(new H2C_CMD)
		val h2c_data				= Flipped(Decoupled(new H2C_DATA))
	})

	//bridge
	{	
		//b
		ToZero(io.axi.b.bits)
		io.axi.b.valid		:= 1.U
		
		//ar and r
		io.axi.ar.ready		:= 1.U
		ToZero(io.axi.r.bits)
		io.axi.r.bits.last	:= 1.U
		io.axi.r.valid		:= 1.U

		//aw and w
		io.axi.aw.ready		:= 1.U

		val q				= XQueue(new AxiBridgeData, TODO_512)//must be large enough, otherwise bridge write may fail, num_w_fire < num_aw_fire
		// Connection.one2one(q.io.in)(io.axi.w)
		q.io.in.valid		:= io.axi.w.valid
		io.axi.w.ready		:= 1.U //never block w data, just report conjection
		q.io.in.bits.data	<> io.axi.w.bits.data
		q.io.out			<> io.axib_data
		val axib_w_lost_num	= Statistics.count(io.axi.w.valid && ~q.io.in.ready)
		Collector.report(axib_w_lost_num)

		val q_free_slots	= (TODO_512.U - q.io.count).asTypeOf(UInt(32.W))
		Collector.report(q_free_slots,fix_str = "REG_FREE_SLOTS")

	}

	//h2c
	{
		ToZero(io.h2c_cmd.bits)
		Connection.one2one(io.h2c_cmd)(io.readCMD)
		io.h2c_cmd.bits.addr	:= io.readCMD.bits.addr_offset + io.start_addr
		io.h2c_cmd.bits.len		:= io.readCMD.bits.len
		io.h2c_cmd.bits.sop		:= 1.U
		io.h2c_cmd.bits.eop		:= 1.U
		io.h2c_cmd.bits.qid		:= 0.U

		val q_h2c_data			= XQueue(new ReadData, TODO_64)
		Connection.one2one(q_h2c_data.io.in)(io.h2c_data)
		q_h2c_data.io.in.bits.data	:= io.h2c_data.bits.data
		q_h2c_data.io.in.bits.last	:= io.h2c_data.bits.last
		io.readData			<> q_h2c_data.io.out

		//debug
		val max_h2c_data_stall	= Statistics.longestActive(io.h2c_data.valid && !io.h2c_data.ready)
		Collector.report(max_h2c_data_stall)
	}

	//c2h
	{	
		val tags			= RegInit(VecInit(Seq.fill(NUM_Q)(0.U(7.W))))
		when(io.tag_index === (RegNext(io.tag_index)+1.U)){
			tags(RegNext(io.tag_index))	:= io.pfch_tag
		}
		Connection.one2one(io.c2h_cmd)(io.writeCMD)
		ToZero(io.c2h_cmd.bits)
		io.c2h_cmd.bits.qid			:= 0.U
		io.c2h_cmd.bits.pfch_tag	:= tags(0.U)
		io.c2h_cmd.bits.addr		:= io.writeCMD.bits.addr_offset + io.start_addr
		io.c2h_cmd.bits.len			:= io.writeCMD.bits.len
		
		//todo  data:ctrl_qid
		Connection.one2one(io.c2h_data)(io.writeData)
		ToZero(io.c2h_data.bits)
		io.c2h_data.bits.data		<> io.writeData.bits.data
		io.c2h_data.bits.last		<> io.writeData.bits.last

	}
	Collector.fire(io.axi.aw)
	Collector.fire(io.axi.w)

	val qdma_timer = Timer(io.c2h_cmd.fire(), io.axi.aw.fire())
	val qdma_latency = qdma_timer.latency
	val qdma_latency_start_cnt = qdma_timer.cnt_start
	val qdma_latency_end_cnt = qdma_timer.cnt_end

	Collector.report(qdma_latency,fix_str = "REG_QDMA_LATENCY")
	Collector.report(qdma_latency_start_cnt)
	Collector.report(qdma_latency_end_cnt)

	Collector.fire(io.writeCMD)
	Collector.fire(io.writeData)
	Collector.fire(io.c2h_cmd)
	Collector.fire(io.c2h_data)
	Collector.report(io.writeCMD.valid)
	Collector.report(io.writeCMD.ready)
	Collector.report(io.writeData.valid)
	Collector.report(io.writeData.ready)
	Collector.report(io.c2h_cmd.valid)
	Collector.report(io.c2h_cmd.ready)
	Collector.report(io.c2h_data.valid)
	Collector.report(io.c2h_data.ready)
}