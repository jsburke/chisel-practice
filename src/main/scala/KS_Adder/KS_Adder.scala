
package KS_Adder

import chisel3._
import chisel3.util._

class KS_Adder (val width: Int, val useCarry: Boolean) extends Module {
  val io = IO(new Bundle{
    val a        = Input(UInt(width.W))
    val b        = Input(UInt(width.W))
    val carryIn  = if (useCarry) Some(Input(UInt(1.W))) else None
    val sum      = Output(UInt(width.W))
    val carryOut = /*if (useCarry) Some(*/Output(UInt(1.W))/*) else None */
  })

  val carry_0 = io.carryIn.getOrElse(0.U)  // carry lsb is either input or hardwire zero


  // simple shortenings in KS
  def propagate(i: Int): UInt = io.a(i) ^ io.b(i)
  def generate(i: Int):  UInt = io.a(i) & io.b(i)

  // genMids derives the second to second-to-last terms in carries where i >= 1
  // (carry_1 really has no mids, so this returns an empty Seq to be reduced with the others in carry(i))
  def genMids(i: Int): Seq[UInt] = {
    require(i > 0, "genMids cannot be invoked with i <= 0")

    def midGuts(i: Int, prev: Option[UInt]): Seq[UInt] = {
      if (i == 1) Seq()  // base case gives empty Seq, works for all i, even C_1 which has no Mids
      else {
        val newPrev = prev.getOrElse(1.U) & propagate(i - 1)
        Seq(newPrev & generate(i - 2)) ++ midGuts(i - 1, Some(newPrev))
      }
    }

  midGuts(i, None)
  }

  // genTail will produce the last term of a given carry i >= 0
  def genTail(i: Int): UInt = {
    require(i > 0, "genTail cannot be invoked with i <= 0")
    if(i == 1) propagate(0) & carry_0 // base case
    else propagate(i - 1) & genTail(i - 1)
  }

  def carry(i: Int): UInt = {
    if (i == 0) carry_0
    else if (i == 1) generate(0) | (propagate(0) & carry_0)
    else Seq(generate(i - 1)) ++ genMids(i) ++ Seq(genTail(i)) reduceLeft(_|_)
  }

  // internal carry and sums signals
  val intCarry = Wire(Vec(width + 1, UInt(1.W)))
  val intSum   = Wire(Vec(width,     Bool()))

  // carry lsb is special
  intCarry(0)   := carry_0
  intSum(0)     := io.a(0) ^ io.b(0) ^ carry_0

  for(i <- 1 until width) {
    intCarry(i) := carry(i)
    intSum      := propagate(i) ^ intCarry(i - 1)
  }
  
  // drive outputs  
  io.sum := intSum.asUInt
  io.carryOut := carry(width + 1)
}
