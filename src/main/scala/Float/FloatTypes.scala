
package float

import chisel3._
import chisel3.util.Cat

//////////////////////////////////////////////////////
//
// float : defining an easily variable encoding for
//         floating point values
//
//////////////////////////////////////////////////////

// options for Not-a-Number

trait HasSplitNaN {
  val sigNan = Reg(Bool())
  val qNaN   = Reg(Bool())
}

trait HasUnifiedNaN {
  val NaN    = Reg(Bool())
}

// options for zero

trait HasSplitZero {
  val posZero = Reg(Bool())
  val negZero = Reg(Bool())
}

trait HasUnifiedZero {
  val zero    = Reg(Bool())
}

// options to extend floating point definitions

trait FloatOptions {
  val subNorm = Reg(Bool())

  val posInf  = Reg(Bool())
  val negInf  = Reg(Bool())
}

trait HasSimpleOptions  extends FloatOptions with HasUnifiedNaN with HasUnifiedZero
trait HasFancyOptions   extends FloatOptions with HasSplitNaN   with HasSplitZero

class floatBase(exp_width: Int, mant_width: Int) extends Bundle {
  val sign     = Reg(Bool())
  val exponent = Reg(UInt(exp_width.W))
  val mantissa = Reg(UInt(mant_width.W))
}

class floatSimple(exp_width: Int, mant_width: Int) extends floatBase(exp_width, mant_width) with HasSimpleOptions
class floatFancy(exp_width: Int, mant_width: Int)  extends floatBase(exp_width, mant_width) with HasFancyOptions


