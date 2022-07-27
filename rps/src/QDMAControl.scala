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

class QDMAControl extends Module{
	val NUM_Q = 4
	def TODO_32 = 32
	def NUM_AXIB_TYPE = 4
	val NUM_AXIB_TYPE_BITS = log2Up(NUM_AXIB_TYPE)

	val io = IO(new Bundle{
		val pfch_tag				= Input(UInt(32.W))
		val tag_index				= Input(UInt(32.W))
		val start_addr				= Input(UInt(64.W))

		val meta2host				= Flipped(Decoupled(new Meta2Host))
		val data2host				= Flipped(Decoupled(new Data2Host))

		val metaFromHost			= Decoupled(new MetaFromHost)

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

		val q				= XQueue(new MetaFromHost, TODO_32)
		Connection.one2one(q.io.in)(io.axi.w)
		q.io.in.bits.data	<> io.axi.w.bits.data
		q.io.out			<> io.metaFromHost
	}

	//h2c is useless
	{
		ToZero(io.h2c_cmd.bits)
		io.h2c_cmd.valid	:= 0.U
		io.h2c_data.ready	:= 0.U
	}

	//c2h
	{	
		val tags			= RegInit(VecInit(Seq.fill(NUM_Q)(0.U(7.W))))
		when(io.tag_index === (RegNext(io.tag_index)+1.U)){
			tags(RegNext(io.tag_index))	:= io.pfch_tag
		}

		//todo   cmd:addr/len/qid/pfch_tag
		Connection.one2one(io.c2h_cmd)(io.meta2host)
		ToZero(io.c2h_cmd.bits)
		io.c2h_cmd.bits.qid			:= 0.U//todo qid == 0
		io.c2h_cmd.bits.pfch_tag	:= tags(0.U)
		io.c2h_cmd.bits.addr		:= io.meta2host.bits.addr_offset + io.start_addr
		io.c2h_cmd.bits.len			:= io.meta2host.bits.len
		
		//todo  data:ctrl_qid
		Connection.one2one(io.c2h_data)(io.data2host)
		ToZero(io.c2h_data.bits)
		io.c2h_data.bits.data		<> io.data2host.bits.data
		io.c2h_data.bits.last		<> io.data2host.bits.last

	}
}