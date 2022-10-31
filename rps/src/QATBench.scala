package rps

import chisel3._
import chisel3.util._
import qdma._
import common.connection.XArbiter
import common.connection.SimpleRouter
import common.storage.XQueue
import common.connection.Connection
import common.BaseILA
import common.Collector

class QATBench() extends Module{

	def TODO_64 = 64
	def TODO_RAW_PACKET_SIZE 		= 4096 //byte
	def TODO_COMPRESS_SIZE			= 2048
	val io = IO(new Bundle{
		val axib					= Flipped(new AXIB)

		val start_addr				= Input(UInt(64.W))
		val pfch_tag				= Input(UInt(32.W))
		val tag_index				= Input(UInt(32.W))

		val c2h_cmd					= Decoupled(new C2H_CMD)
		val c2h_data				= Decoupled(new C2H_DATA) 
		val h2c_cmd					= Decoupled(new H2C_CMD)
		val h2c_data				= Flipped(Decoupled(new H2C_DATA))
	})

	val qdma_control	= {
		val t = Module(new QDMAControl)
		t.io.axi		<> io.axib
		t.io.c2h_cmd	<> io.c2h_cmd
		t.io.c2h_data	<> io.c2h_data
		t.io.h2c_cmd	<> io.h2c_cmd
		t.io.h2c_data	<> io.h2c_data
		t.io.pfch_tag	<> io.pfch_tag
		t.io.tag_index	<> io.tag_index
		t.io.start_addr	<> io.start_addr
		t
	}

	val q_read_cmd	= XQueue(new ReadCMD, TODO_64)
	val q_write_cmd	= XQueue(new WriteCMD, TODO_64)
	val axib_data	= qdma_control.io.axib_data
	Connection.one2many(qdma_control.io.axib_data)(q_read_cmd.io.in,q_write_cmd.io.in)
	q_read_cmd.io.in.bits.addr_offset	:= axib_data.bits.data(31,0)
	q_read_cmd.io.in.bits.len			:= TODO_RAW_PACKET_SIZE.U
	q_read_cmd.io.out					<> qdma_control.io.readCMD

	q_write_cmd.io.in.bits.addr_offset	<> axib_data.bits.data(63,32)
	q_write_cmd.io.in.bits.len			<> TODO_COMPRESS_SIZE.U
	q_write_cmd.io.out					<> qdma_control.io.writeCMD

	val accelerator 					= Module(new Accelerator)
	accelerator.io.in					<> qdma_control.io.readData
	accelerator.io.out					<> qdma_control.io.writeData

	class ila_bench(seq:Seq[Data]) extends BaseILA(seq)
  	val mod_bench = Module(new ila_bench(Seq(	
		qdma_control.io.axib_data,
		qdma_control.io.readCMD,
		qdma_control.io.writeCMD,
		qdma_control.io.readData,
		qdma_control.io.writeData,

		qdma_control.io.c2h_cmd,
		qdma_control.io.h2c_cmd,
  	)))
  	mod_bench.connect(clock)

	Collector.fire(accelerator.io.in)
	Collector.fireLast(accelerator.io.in)
	Collector.fire(accelerator.io.out)
	Collector.fireLast(accelerator.io.out)
	Collector.fire(q_read_cmd.io.in)
	Collector.fire(q_read_cmd.io.out)
	Collector.fire(q_write_cmd.io.in)
	Collector.fire(q_write_cmd.io.out)

	Collector.report(accelerator.io.in.valid)
	Collector.report(accelerator.io.in.ready)
	Collector.report(accelerator.io.out.valid)
	Collector.report(accelerator.io.out.ready)
}


class Accelerator() extends Module{
	def L1 = 4
	def L2 = 12
	val io = IO(new Bundle{
		val in	= Flipped(Decoupled(new CompressData))
		val out = Decoupled(new CompressData)
	})

	val compressUnits	= Seq.fill(L1*L2)(Module(new CompressUnit()))

	val routerL1		= SimpleRouter(new CompressData, L1)
	val routerL2		= Seq.fill(L1)(SimpleRouter(new CompressData,L2))

	val idx	= RegInit(UInt((log2Up(L1)).W), 0.U)
	when(io.in.fire() && io.in.bits.last===1.U){
		idx			:= idx + 1.U
		when(idx+1.U === L1.U){
			idx		:= 0.U
		}
	}
	routerL1.io.in		<> io.in
	routerL1.io.idx		<> idx

	for(i<-0 until L1){
		val idx	= RegInit(UInt((log2Up(L2)).W), 0.U)
		when(routerL1.io.out(i).fire() && routerL1.io.out(i).bits.last===1.U){
			idx			:= idx + 1.U
			when(idx+1.U === L2.U){
				idx		:= 0.U
			}
		}
		routerL2(i).io.idx	<> idx
		routerL1.io.out(i)	<> routerL2(i).io.in
		for(j<-0 until L2){
			routerL2(i).io.out(j)	<> compressUnits(i*L2+j).io.in
		}
	}

	val arbiter			= XArbiter(Seq(L1,L2))(compressUnits.map(_.io.out),io.out)
}