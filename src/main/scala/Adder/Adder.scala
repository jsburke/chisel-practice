
package Adder

import chisel3._

////////////////////////////////////////////////////////////////
//
//  Full Adder : This example shows a very basic "Hello World"
//               sort of module in Chisel.  No parameters, no
//               extra signals explicitly declared internally,
//               and a simple well understood piece of hardware
//
////////////////////////////////////////////////////////////////

class FullAdder extends Module {  // example of making a module with no parameters for the constructor
  val io = IO(new Bundle{
    // First declaring the inputs
    val a    = Input(UInt(1.W))      // setting to one bit UInt for ease
    val b    = Input(UInt(1.W))
    val cin  = Input(UInt(1.W))
    // Now declaring the outputs
    val sum  = Output(UInt(1.W))
    val cout = Output(UInt(1.W))
  })

  // use the inputs plus logic primitives to generate output
  io.sum  := io.a ^ io.b ^ io.cin  // xor inputs
  io.cout := (io.a & io.b) | (io.a & io.cin) | (io.b & io.cin) // and all pairs, then or the results
}

////////////////////////////////////////////////////////////////
//
//  Adder :  Slightly more advanced than the Full Adder, this
//           module shows how to set up a parameterized constructor.
//           It also uses the above simple module as a submodule
//           and introduces "arrays" of modules and signals
//
////////////////////////////////////////////////////////////////

// NB!! If you want to access a constructor parameter like num_sz, it must be declared as a val to be accessed!
// otherwise later modules or testers using it cannot access it, ie Adder.num_sz will raise an error!

class Adder(val num_sz: Int) extends Module {  // Ripple-Carry Adder showing parameterization and using submodules
  val io = IO(new Bundle{
    val in1  = Input(UInt(num_sz.W))
    val in2  = Input(UInt(num_sz.W))
    val cin  = Input(UInt(1.W))
    val sum  = Output(UInt(num_sz.W))
    val cout = Output(UInt(1.W))
  })

  println("Adder made with " + num_sz + " bits")

  val subAdders  = Array.fill(num_sz)(Module(new FullAdder()).io)  //adders is an array of FullAdder Modules io
  val subCarries = Wire(Vec(num_sz+1, UInt(1.W))) // carries between modules, array of one bit UInts
  val subSums    = Wire(Vec(num_sz, Bool()))   // vec of bools so that we can convert to UInt later

  // Handle the non-typical case of the lowest order carry
  subCarries(0)      := io.cin

  // use a for loop to connect signals easily based off index
  for(i <- 0 until num_sz){  // C equivalent : for(int i = 0; i < num_sz; i++) { ... }
    subAdders(i).a    := io.in1(i)  // connect the module input to the full adder input
    subAdders(i).b    := io.in2(i)
    subAdders(i).cin  := subCarries(i)
    subCarries(i + 1) := subAdders(i).cout
    subSums(i)        := subAdders(i).sum
  }

  io.sum  := subSums.asUInt      // Whole array to output
  io.cout := subCarries(num_sz)  // Highest order carry out
}
