package project_foo
import chisel3._
import chisel3.util._

class Package extends Bundle{
	val data = UInt(32.W)
	val last = UInt(1.W)
}

class Test extends Module{
	val io = IO(new Bundle{
		val in	= Flipped(Decoupled(new Package))
		val out = Decoupled(new Package)
	})
	io.out	<> io.in
}