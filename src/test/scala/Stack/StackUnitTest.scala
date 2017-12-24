
package Stack

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

// instantiate a dummy class for testing
class UIntStack(val width: Int, depth: Int) extends Stack(UInt(width.W), depth)

class StackUnitTest(c: UIntStack) extends PeekPokeTester(c) {
  // going to be a very dry, directed test

  poke(c.io.in.write_enable, true)
  poke(c.io.in.read_enable, true)
  poke(c.io.in.flush, false)
  poke(c.io.in.write_data, rnd.nextInt(c.width))
  step(1)
  poke(c.io.in.read_enable, false)
  step(1)
  poke(c.io.in.write_data, rnd.nextInt(c.width))
  step(6)
  poke(c.io.in.write_enable, false)
  poke(c.io.in.read_enable, true)
  step(5)
}

class StackTester extends ChiselFlatSpec {
  val w = 8 // test width
  val d = 4 // test depth

  private val backendNames = if(firrtl.FileUtils.isCommandAvailable("verilator")) {
    Array("firrtl","verilator")
  }
  else { Array("firrtl") }

  "using --backend-name verilator" should "be the only way for now" in {
    if(backendNames.contains("verilator")) {
      iotesters.Driver.execute(Array("--backend-name", "verilator"), () => new UIntStack(w, d)) {
        c => new StackUnitTest(c)
      } should be (true)
    }
  }
}
