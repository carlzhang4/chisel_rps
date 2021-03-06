package rps

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import firrtl.options.TargetDirAnnotation
import qdma._

object elaborate extends App {
	println("Generating a %s class".format(args(0)))
	val stage	= new chisel3.stage.ChiselStage
	val arr		= Array("-X", "sverilog", "--full-stacktrace")
	val dir 	= TargetDirAnnotation("Verilog")

	args(0) match{
		case "RPSTop" => stage.execute(arr,Seq(ChiselGeneratorAnnotation(() => new RPSTop()),dir))
		case "CompressorHBM" => stage.execute(arr,Seq(ChiselGeneratorAnnotation(() => new CompressorHBM()),dir))
		case "HBMCompress" => stage.execute(arr,Seq(ChiselGeneratorAnnotation(() => new HBMCompress(4,12)),dir))
		case _ => println("Module match failed!")
	}
}