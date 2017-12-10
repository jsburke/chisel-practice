
package iterMul

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class iterMulUnitTest(c: iterMul) extends PeekPokeTester(c) {

  for (i <- 0 until 4){
    val r1     = rnd.nextInt(c.op_sz)
    val r2     = rnd.nextInt(c.op_sz)
    var cycles = 1

    poke(c.io.deq.bits.in1, r1)
    poke(c.io.deq.bits.in2, r2)
    poke(c.io.deq.valid, 1)
    poke(c.io.enq.ready, 0)
    step(1)
    poke(c.io.deq.valid, 0)
    poke(c.io.enq.ready, 1)
    
    while(peek(c.io.enq.valid) == BigInt(0)) {
      step(1)
      cycles += 1
    }
    expect(c.io.enq.bits.product, r1 * r2)
    println(s"Multiplying $r1 and $r2 took $cycles cycles")
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
