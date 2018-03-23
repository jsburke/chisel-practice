
package iterMul

import chisel3._
import chisel3.util._

class shifterIn (val size: Int) extends Bundle {

  private val shift_sz = log2Ceil(size)

  val value = UInt(size.W)
  val shamt = UInt(shift_sz.W)

  override def cloneType = (new shifterIn(size)).asInstanceOf[this.type]
}

class shifterOut (val size: Int) extends Bundle {

  val value = UInt(size.W)

  override def cloneType = (new shifterOut(size)).asInstanceOf[this.type]
}

abstract class shifterAbs (val data_sz: Int) extends Module {

  val io = IO(new Bundle {
    val in  = Flipped(Decoupled(new shifterIn(data_sz)))
    val out = Decoupled(new shifterOut(data_sz))
  })
}

class shifterFMS (val data_sz: Int) extends shifterAbs(data_sz) {

  private val shift_sz = log2Ceil(data_sz)

  // state decls
  val s_Ready :: s_Shift :: s_Done :: Nil = Enum(3)
  val state = RegInit(s_Ready.asUInt)

  io.in.ready  = false.B
  io.out.valid = false.B

  val value = Reg(UInt(op_sz.W))    // value to be shifted
  val shift = Reg(UInt(shift_sz.W)) // value to shift by    
  val shamt = Reg(UInt(shift_sz.W)) // shift per cycle

  // circuitry
  switch(state) {

    is(s_Ready) {
      io.in.ready  := true.B
      io.out.valid := false.B
      value        := io.in.bits.value
      shift        := io.in.bits.shamt
      shamt        := 1.U
      when (io.in.valid) {state := s_Calc}
      .otherwise          {state := s_Ready}
    }

    is(s_Calc) {
      io.in.ready := false.B
      io.out.valid := false.B
      value        := Mux(shift(0), value << shamt, value)
      shift        := shift >> 1;
      shamt        := shamt << 1;
      when (shift === 0.U) {state := s_Done}
      .otherwise           {state := s_Calc}
    }

    is(s_Done) {
      io.in.ready  := false.B
      io.out.valid := true.B
      value        := value
      when (io.out.ready) {state := s_Ready}
      .otherwise          {state := s_Done}
    }

  }

  io.out.bits.value := value
  
}
