
package KS_Adder

import chisel3._
import chisel3.util._

class KS_Adder (val width: Int, val useCarry: Boolean) extends Module {
  val io = IO(new Bundle{
    val a        = Input(UInt(width.W))
    val b        = Input(UInt(width.W))
    val carryIn  = if (useCarry) Some(Input(UInt(1.W))) else None
    val sum      = Output(UInt(width.W))
    val carryOut = if (useCarry) Some(Output(UInt(1.W))) else None
  })

  val carry_0 = io.carryIn.getOrElse(0.U)  // carry lsb is either input or hardwire zero

  def propagate(i: Int): UInt = io.a(i) ^ io.b(i)
  def generate(i: Int):  UInt = io.a(i) & io.b(i)

  def genMids(i: Int): UInt = 1.U // NB clearly dummy for now

  def genTail(i: Int): UInt = {
    require(i > 0, "genTail invoked with i <= 0")
    if(i == 1) propagate(0) & carry_0 // base case
    else propagate(i - 1) & genTail(i - 1)
  }

  def carry(i: Int): UInt = {
    if (i == 0) carry_0
    else if (i == 1) generate(0) | (propagate(0) & carry_0)
    else Seq(generate(i - 1), genMids(i), genTail(i)) reduceLeft(_|_)
  }
}
