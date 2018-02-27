
package KS_Adder

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

//Stream.continually(nextInt(100)).take(10)

class KS_AdderUnitTest(c: KS_Adder) extends PeekPokeTester(c) {

  val base = 1 to 4 
  val in_a = base map (_*2)
  val in_b = base map (_ << 2 + 1)

  for (i <- 0 until 4){
    val a_sel = in_a(i)
    val b_sel = in_b(i) 
    poke(c.io.a, a_sel)
    poke(c.io.b, b_sel)
    step(1)
    val rand_sum = a_sel + b_sel 
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

