
package decoder

import chisel3._
import chisel3.util._

// add notes soon

class InsnBits extends Bundle {
  val bits = Bits(32.W)  // since we know in general RV insn is 32 bits
  val rvc  = Bool()
}
