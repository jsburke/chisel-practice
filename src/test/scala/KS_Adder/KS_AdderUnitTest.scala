
package KS_Adder

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class KS_AdderUnitTest(c: KS_Adder) extends PeekPokeTester(c) {

  for (i <- 0 until 6){
    val rand_in1 = rnd.nextInt(c.width)
    val rand_in2 = rnd.nextInt(c.width)

    poke(c.io.a, rand_in1)
    poke(c.io.b, rand_in2)
    step(1)
    val rand_sum = rand_in1 + rand_in2 
    val mask     = BigInt("1"*c.width, 2) 
    expect(c.io.sum, rand_sum & mask)
//    expect(c.io.cout, rand_sum % 1)
  }
}

class Tester extends ChiselFlatSpec {
  behavior of "KS_Adder"
  backends foreach {backend =>
    it should s"create a vcd file from the test" in {
/*      Driver(() => new KS_Adder(32, false))(c => new KS_AdderUnitTest(c)) should be (true) */
      iotesters.Driver.execute(Array("--fint-write-vcd"), () => new KS_Adder(12,false)) {
        c => new KS_AdderUnitTest(c)
      } should be(true)
    }
  }
}

