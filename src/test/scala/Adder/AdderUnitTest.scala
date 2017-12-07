
package Adder

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class AdderUnitTest(c: Adder) extends PeekPokeTester(c) { // often, like ijk in loops, c is the default for the unit under test

  for (i <- 0 until 4){
    val rand_in1 = rnd.nextInt(c.num_sz)
    val rand_in2 = rnd.nextInt(c.num_sz)
    val rand_cin = rnd.nextInt(1)

    poke(c.io.in1, rand_in1)  // push random values into the inputs
    poke(c.io.in2, rand_in2)
    poke(c.io.cin, rand_cin)
    step(1)
    val rand_sum = rand_in1 + rand_in2 + rand_cin
    val mask     = BigInt("1"*c.num_sz, 2) // Construct a Scala BigInt based on a binary number of num_sz 1s
    expect(c.io.sum, rand_sum & mask) // check results with those calculated by raw scala
    expect(c.io.cout, rand_sum % 1)
  }
}

class AdderTester extends ChiselFlatSpec {
  val width = 16

  private val backendNames = if(firrtl.FileUtils.isCommandAvailable("verilator")) {
    Array("firrtl", "verilator")
  }
  else {
    Array("firrtl")
  }
  for ( backendName <- backendNames ) {
    "Adder" should s"calculate proper sum (with $backendName)" in {
      Driver(() => new Adder(width), backendName) {
        c => new AdderUnitTest(c)
      } should be (true)
    }
  }

  "Basic test using Driver.execute" should "be used as an alternative way to run specification" in {
    iotesters.Driver.execute(Array(), () => new Adder(width)) {
      c => new AdderUnitTest(c)
    } should be (true)
  }

  "using --backend-name verilator" should "be an alternative way to run using verilator" in {
    if(backendNames.contains("verilator")) {
      iotesters.Driver.execute(Array("--backend-name", "verilator"), () => new Adder(width)) {
        c => new AdderUnitTest(c)
      } should be(true)
    }
  }

  "running with --is-verbose" should "show more about what's going on in your tester" in {
    iotesters.Driver.execute(Array("--is-verbose"), () => new Adder(width)) {
      c => new AdderUnitTest(c)
    } should be(true)
  }

  "running with --fint-write-vcd" should "create a vcd file from your test" in {
    iotesters.Driver.execute(Array("--fint-write-vcd"), () => new Adder(width)) {
      c => new AdderUnitTest(c)
    } should be(true)
  }

  "using --help" should s"show the many options available" in {
    iotesters.Driver.execute(Array("--help"), () => new Adder(width)) {
      c => new AdderUnitTest(c)
    } should be (true)
  }
}

//  below is a less verbose adder tester
//
//class AdderTester extends ChiselFlatSpec {
//  behavior of "Adder"
//  backends foreach {backend =>
//    it should s"correctly add randomly generated numbers $backend" in {
//      Driver(() => new Adder(32))(c => new AdderUnitTest(c)) should be (true)
//    }
//  }
//}

