package rps

import chisel3._
import chisel3.util._
import common.axi.HasLast

class RecvMeta extends Bundle{
	val addr = Output(UInt(64.W))//len is fixed
	val src = Output(UInt(32.W))
	val dest = Output(UInt(32.W))//to which qp
	val rpc_field = Output(UInt(128.W))
}

class Meta2Host extends Bundle{
	val addr_offset = Output(UInt(32.W))
	val len	= Output(UInt(16.W))
}

class Data2Host extends Bundle with HasLast{
	val data = Output(UInt(512.W))
}

class Meta2SSD extends Bundle{
	val data = Output(UInt(512.W))
}

class MetaFromHost extends Bundle{
	val data = Output(UInt(512.W))
}

class Meta2Client extends Bundle{
	val data = Output(UInt(512.W))
}

class Meta2Compressor extends Bundle{
	val addr = Output(UInt(64.W))
}

class CompressData extends Bundle with HasLast{
	val data		= Output(UInt(512.W))
}