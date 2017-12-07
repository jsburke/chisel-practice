
package iterMul

import chisel3._
import chisel3.util._

////////////////////////////////////////////////////////////////
//
//  iterMul : this is a basic fixed latency shift-add integer
//            multiplier with three states (one superfluous)
//
//            Introduce Decoupled IO extension
//            State machines in Chisel
//
////////////////////////////////////////////////////////////////

class iterMulIn (val size: Int) extends Bundle {
  val in1 = UInt(size.W)
  val in2 = UInt(size.W)

  // need cloneType method override for use
  override def cloneType = (new iterMulIn(size)).asInstanceOf[this.type]
}



class iterMulOut (val size: Int) extends Bundle {
  val product = UInt(size.W)  // will be double that of iterMulIn's in1 and in2

  override def cloneType = (new iterMulOut(size)).asInstanceOf[this.type]
}



class iterMul (val op_sz: Int) extends Module {
  
  val io = IO(new Bundle {
    val deq = Flipped(Decoupled(new iterMulIn(op_sz)))
    val enq = Decoupled(new iterMulOut(op_sz))
  })

  // declare state related stuff
  val s_Ready :: s_Calc :: s_Done :: Nil = Enum(UInt(), 3)
  val state = Reg(init = s_Ready)

  // default output states before switch to use last connect semantics
  io.deq.ready := false.B
  io.enq.valid := false.B //imitates Calc state

  // control to datapath
  //val mux_sel = Wire(Bool())
 
  val a       = Reg(UInt(op_sz.W))
  val b       = Reg(UInt(op_sz.W))
  val partial = Reg(UInt(op_sz.W))


  // state logic
  switch(state) {

    is(s_Ready) {
      io.deq.ready := true.B
      io.enq.valid := false.B
      partial      := 0.U
      a            := io.deq.bits.in1
      b            := io.deq.bits.in2 
      when (io.deq.valid) {state := s_Calc}
      .otherwise          {state := s_Ready}
    }

    is(s_Calc) {
      io.deq.ready := false.B
      io.enq.valid := false.B
      //mux_sel      := true.B  // choose shifted vals
      when (b.orR === 0.U) {state := s_Done}
      .otherwise           {state := s_Calc}
    }

    is(s_Done) {
      io.deq.ready := false.B
      io.enq.valid := true.B
      //mux_sel      := true.B // don't choose inputs
      when (io.enq.ready) {state := s_Ready}
      .otherwise          {state := s_Done}
    }

  }

  // datapath
  //val a       = Reg(next = Mux(mux_sel, a << 1, io.deq.bits.in1))
  //val b       = Reg(next = Mux(mux_sel, b >> 1, io,deq.bits.in2))
  //val partial = Reg(next = Mux(mux_sel, Mux(b(0), partial + a, partial), 0.U)) 
  io.enq.bits.product := partial
}
