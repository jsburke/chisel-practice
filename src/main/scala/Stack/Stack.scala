
package Stack

import chisel3._
import chisel3.util.log2Ceil

/////////////////////////////////////////////////////////////////
//
// Stack : basic Stack of parametrizable depth
//         first value in is last out
//
//         introduces some simple polymorphism
//
/////////////////////////////////////////////////////////////////

class StackIn[T <: Data](gen: T) extends Bundle{
  val read_enable  = Input(Bool())
  val write_enable = Input(Bool())
  val write_data   = Input(gen.chiselCloneType)
  val flush        = Input(Bool())
}

class StackOut[T <: Data](gen: T) extends Bundle{
  val read_data = Output(gen.chiselCloneType)
  val full      = Output(Bool())
  val empty     = Output(Bool())
}

class Stack[T <: Data](gen: T, val depth: Int) extends Module{
  val io = IO(new Bundle{
    val in  = new StackIn(gen)
    val out = new StackOut(gen)
  })

  val stack_mem   = Mem(depth, gen)
  val p_stack     = Reg(init = UInt(0, depth)) // should make own class for inc, dec, max, etc other methods for ease
  val stack_empty = Reg(init = Bool(true))
  val stack_full  = Reg(init = Bool(false))
  val data_out    = Reg(gen)

  when(io.in.flush) {
    p_stack     := 0.U
    stack_empty := true.B
    data_out    := stack_mem(0.U)
  }

  when(stack_empty) {
    when(io.in.read_enable && io.in.write_enable) {
      data_out := io.in.write_data // pass through on empty
    }
    .elsewhen(io.in.read_enable) {
      data_out := stack_mem(0.U) // give a dummy value
    }
    .elsewhen(io.in.write_enable) {
      data_out := stack_mem(0.U)
      stack_empty := false.B
      stack_mem(p_stack) := io.in.write_data
      p_stack     := p_stack + 1.U
    }
  }
  .otherwise { // not empty
    when(io.in.read_enable && io.in.write_enable) {
      data_out := io.in.write_data  // pass through, not DRY
    }
    .elsewhen(io.in.read_enable) {
      data_out := stack_mem(p_stack)
      stack_empty := (p_stack - 1.U) === 0.U
      p_stack     := p_stack - 1.U
      stack_full  := false.B  // can guarantee
    }
    .elsewhen(io.in.write_enable) {
      stack_full         := (p_stack + 1.U) === UInt(depth)
      stack_mem(p_stack) := io.in.write_data
      p_stack            := p_stack + 1.U
    }
  }
  
  io.out.full      := stack_full
  io.out.empty     := stack_empty
  io.out.read_data := data_out
}



