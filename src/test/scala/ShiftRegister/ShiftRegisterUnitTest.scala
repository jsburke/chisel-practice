
package ShiftRegister

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

// this test presently fails with negatives

class ShiftRegisterUnitTest(c: ShiftRegister) extends PeekPokeTester(c) {

  def sr_calc(width: Int, new_lsb: Int, prev: Int): Int = {  // being lazy with new_lsb, don't know how to generate random boolean
    val mask = if(prev < 0) -1 else(1 << (width + 1)) - 1  // keep in mind this is not hardware, use this method for higher order zeros
    val lsb  = if(new_lsb == 1) 1 else 0
    val res  = ((prev & mask) << 1) + lsb
    res
  }

  var prev   = 0
  val enable = 1
  for (i <- 0 until 100){
    val rand_lsb = rnd.nextInt(1)
    poke(c.io.enable, enable)
    poke(c.io.new_lsb, rand_lsb)
    step(1)
    expect(c.io.out, sr_calc(c.reg_len, rand_lsb, prev))
  }
}



class ShiftRegisterTester extends ChiselFlatSpec {
  behavior of "ShiftRegister"
  backends foreach {backend =>
    it should s"correctly generate the shifted numbers $backend" in{
      Driver(() => new ShiftRegister(8))(c => new ShiftRegisterUnitTest(c)) should be (true)
    }
  }
}
