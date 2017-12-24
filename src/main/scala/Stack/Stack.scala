
package FIFO

import chisel3._

/////////////////////////////////////////////////////////////////
//
// FIFO : basic FIFO of parametrizable depth
//
//        introduces some simple polymorphism
//
/////////////////////////////////////////////////////////////////

class FIFOIn[T <: Data](gen: T) extends Bundle{
  val read_enable  = Input(Bool)
  val write_enable = Input(Bool)
  val write_data   = Input(gen)
  val flush        = Input(Bool)
}

class FIFOOut[T <: Data](gen: T) extends Bundle{
  val read_data = Output(gen)
  val full      = Output(Bool)
  val empty     = Output(Bool)
}

class FIFO[T <: Data](gen: T, val depth: Int) extends Module{
  val io = IO(new Bundle{
    val in  = FIFOIn(gen)
    val out = FIFOOut(gen)
  })

  val fifo_mem   = Mem(depth, gen)
  val p_write    = Reg(init = UInt(0, depth))
  val p_read     = Reg(init = UInt(0, depth))
  val fifo_empty = Reg(init = Bool(true))

}
