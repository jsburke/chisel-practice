
package float

import chisel3._
import chisel3.util.Cat

//////////////////////////////////////////////////////
//
// float : defining an easily variable encoding for
//         floating point values
//
//////////////////////////////////////////////////////

// Not-a-Number

trait HasNaN {
  val NaN    = Bool()
}

// options for zero

trait HasSplitZero {
  val posZero = Bool()
  val negZero = Bool()
}

trait HasUnifiedZero {
  val zero    = Bool()
}

// options to extend floating point definitions

trait FloatOptions {
  val subNorm = Bool()

  val posInf  = Bool()
  val negInf  = Bool()
}

trait HasSimpleOptions  extends FloatOptions with HasUnifiedNaN with HasUnifiedZero
trait HasFancyOptions   extends FloatOptions with HasSplitNaN   with HasSplitZero

class floatBase(exp_width: Int, mant_width: Int) extends Bundle {
  val sign     = Bool()
  val exponent = UInt(exp_width.W)
  val mantissa = UInt(mant_width.W)

  override def cloneType = new floatBase(exp_width, mant_width).asInstanceOf[this.type]
}

class floatExtended(exp_width: Int, mant_width: Int) extends floatBase(exp_width, mant_width) {
  // deal with zeros
  def zero:    Bool = ~(exponent.orR | mantissa.orR)
  def posZero: Bool = ~(sign | zero)
  def negZero: Bool = ~(~sign | zero)

  // deal with infinities
  def inf: Bool     = exponent.andR & ~(mantissa.orR)
  def posInf: Bool  = ~sign & inf
  def negInf: Bool  = sign & inf
 
  def NaN: Bool     = exponent.andR & mantissa.orR
  def denorm: Bool  = ~(exponent.andR) & mantissa.orR
}
