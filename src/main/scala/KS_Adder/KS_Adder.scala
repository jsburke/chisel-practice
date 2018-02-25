
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


  // simple shortenings in KS
  def propagate(i: Int): UInt = io.a(i) ^ io.b(i)
  def generate(i: Int):  UInt = io.a(i) & io.b(i)

  // genMids derives the second to second-to-last terms in carries where i > 1
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

  
}
