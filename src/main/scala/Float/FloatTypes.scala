
package float

import chisel3._

//////////////////////////////////////////////////////
//
// float : defining an easily variable encoding for
//         floating point values
//
//////////////////////////////////////////////////////

class floatBase(exp_width: Int, mant_width: Int) extends Bundle {
  val sign     = Bool()
  val exponent = UInt(exp_width.W)
  val mantissa = UInt(mant_width.W)

  override def cloneType = new floatBase(exp_width, mant_width).asInstanceOf[this.type]
}

class floatExtended(exp_width: Int, mant_width: Int) extends floatBase(exp_width, mant_width) {
  // deal with zeros
  val zero    = ~(exponent.orR | mantissa.orR)
  val posZero = ~(sign | zero)
  val negZero = ~(~sign | zero)

  // deal with infinities
  val inf     = exponent.andR & ~(mantissa.orR)
  val posInf  = ~sign & inf
  val negInf  = sign & inf
 
  val NaN     = exponent.andR & mantissa.orR
  val denorm  = ~(exponent.andR) & mantissa.orR

  override def cloneType = new floatExtended(exp_width, mant_width).asInstanceOf[this.type]
}
