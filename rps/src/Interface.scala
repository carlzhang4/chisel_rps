package rps

import chisel3._
import chisel3.util._
import common.axi.HasLast

class ReadCMD extends Bundle{
	val addr_offset = Output(UInt(32.W))
	val len	= Output(UInt(16.W))
}

class ReadData extends Bundle with HasLast{
	val data 	= Output(UInt(512.W))
}

class WriteCMD extends Bundle{
	val addr_offset = Output(UInt(32.W))
	val len	= Output(UInt(16.W))
}

class WriteData extends Bundle with HasLast{
	val data = Output(UInt(512.W))
}

class AxiBridgeData extends Bundle{
	val data = Output(UInt(512.W))
}

class Meta2Compressor extends Bundle{
	val addr = Output(UInt(64.W))
}

class CompressData extends Bundle with HasLast{
	val data		= Output(UInt(512.W))
}