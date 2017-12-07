
package iterMul

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, OrderedDecoupledHWIOTester}

class iterMulUnitTest extends OrderedDecoupledHWIOTester {
 
  val device_under_test = Module(new iterMul(32))  // don't like this here, prefer in flat spec
  val c = device_under_test

  for (i <- 0 until 4) {
    val r_in1 = rnd.nextInt(c.op_sz)
    val r_in2 = rnd.nextInt(c.op_sz)
    
    println("hello from loop")

    inputEvent(c.io.deq.bits.in1 -> r_in1, c.io.deq.bits.in2 -> r_in1)
    //outputEvent(c.io.enq.bits.product -> r_in1 * r_in2)
  }
}

class iterMulTester extends ChiselFlatSpec {
  val width = 16

  private val backendNames = if(firrtl.FileUtils.isCommandAvailable("verilator")) {
    Array("firrtl", "verilator")
  }
  else {
    Array("firrtl")
  }
  for ( backendName <- backendNames ) {
    "iterMul" should s"calculate proper sum (with $backendName)" in {
      Driver(() => new iterMul(width), backendName) {
        c => new iterMulUnitTest(c)
      } should be (true)
    }
  }

  "Basic test using Driver.execute" should "be used as an alternative way to run specification" in {
    iotesters.Driver.execute(Array(), () => new iterMul(width)) {
      c => new iterMulUnitTest(c)
    } should be (true)
  }

  "using --backend-name verilator" should "be an alternative way to run using verilator" in {
    if(backendNames.contains("verilator")) {
      iotesters.Driver.execute(Array("--backend-name", "verilator"), () => new iterMul(width)) {
        c => new iterMulUnitTest(c)
      } should be(true)
    }
  }

  "running with --is-verbose" should "show more about what's going on in your tester" in {
    iotesters.Driver.execute(Array("--is-verbose"), () => new iterMul(width)) {
      c => new iterMulUnitTest(c)
    } should be(true)
  }

  "running with --fint-write-vcd" should "create a vcd file from your test" in {
    iotesters.Driver.execute(Array("--fint-write-vcd"), () => new iterMul(width)) {
      c => new iterMulUnitTest(c)
    } should be(true)
  }

  "using --help" should s"show the many options available" in {
    iotesters.Driver.execute(Array("--help"), () => new iterMul(width)) {
      c => new iterMulUnitTest(c)
    } should be (true)
  }
}


//class iterMulTester extends ChiselFlatSpec {
//  behavior of "iterMul"
//  backends foreach {backend =>
//    it should s"multiply two random numbers" in {
//      assertTesterPasses {new iterMulUnitTest}      
//    }
//  }
//}
