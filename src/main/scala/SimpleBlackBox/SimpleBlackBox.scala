
package SimpleBlackBox

import chisel3._
import chisel3.experimental._

/////////////////////////////////////////////////////////////
//
//  black box : basic test of Chisel's black box
//              these let you include verilog into
//              chisel sources.  Often used for things 
//              clumsy in chisel like managing clocks
//
//              this module only aims to show a simple
//              implementation of black boxes.  could
//              easily be a chisel only module
//
/////////////////////////////////////////////////////////////

class BlackBoxAdder extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle{
    val in  = Input(UInt(128.W))  // concat of in1 an in2, no rhyme or reason
    val out = Output(UInt(64.W))
  })

  setResource("/simple_adder.v")
}
