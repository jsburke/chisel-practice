
package ShiftRegister

import chisel3._
import chisel3.util.Cat

////////////////////////////////////////////////////////////////
//
//  Shift Register :  The goal of this module is to 
//                    continue from the adder and introduce
//                    sequential elements to the deign
//
////////////////////////////////////////////////////////////////

class ShiftRegister(val reg_len: Int) extends Module {
  val io = IO(new Bundle{
    val new_lsb = Input(Bool())
    val enable  = Input(Bool())
    val out     = Output(UInt(reg_len.W))
  })

  val shiftReg = RegInit(0.U(reg_len.W))  // where the value gets stored

  //  logic below will shift in the new_lsb on every clock posedge
  //  when the enable input is high

  when (io.enable){
    val nextShiftReg = Cat(shiftReg(reg_len - 2, 0), io.new_lsb)  // concatenate the old shift reg with the new lsb, minus its msb
    shiftReg        := nextShiftReg  // on every cycle, push in the new value
  }

  //  assign the output to always be the internal shiftReg
  io.out := shiftReg
}

object ShiftRegisterVerilog extends App {
  chisel3.Driver.execute(args, () => new ShiftRegister(16))
}
