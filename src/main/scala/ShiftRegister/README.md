# Shift Register

This directory contains the Chisel for a basic Shifter Register.  It aims to build on the [Adder](../Adder/Adder.scala) by introducing basic sequential elements from Chisel.

Internally, it starts out as a register containing zero, and every cycle when `enable` is high, the `new_lsb` is shifted in as the new least significant bit.  The current value of the internal register is always produced as an output.

## How to Use

Much like the adder, it only takes a simple call to test this on the command line

```sh
sbt 'testOnly ShiftRegister.ShiftRegisterTester'
```

However, in the interest of minds curious that desire to see Verilog, an extra singleton has been added that will generate Verilog, which can be invoked on the command line as follows

```sh
sbt 'run-main ShiftRegister.ShiftRegisterVerilog'
```

This will put the generated verilog (and firrtl) into whatever directory this is invoked from.  Feel free to look at it, but be forewarned that it may not be a pleasant experience.  The code sets it up to be a 16 bit shift register

## Chisel Explanation

This explanation should be a bit more brief than the Adder since you should now have at least a little familiarity with Chisel given the previous Adder example.  The big new topics at hand this time are sequential elements in Chisel and the associated structures to change elements on clock edges.

### Inside the Chisel

We start this much the same as the previous Adder example:

```scala
package ShiftRegister

import chisel3._
import chisel3.util.Cat
```

Like last time, we're setting up a new package and importing chisel.  However, this time we are also importing the `Cat` function for chisel's util.  This is the general concatenation method in chisel, and it lets us make the shift register easily.

After the comment stating in general what's going on:

```scala
class ShiftRegister(val reg_len: Int) extends Module {
  val io = IO(new Bundle{
    val new_lsb = Input(Bool())
    val enable  = Input(Bool())
    val out     = Output(UInt(reg_len.W))
  })
```

Once again, we make a new module that comes with a parameter for object creation.  A Bundle is used to make the inputs and outputs.  The `new_lsb` is the bit we shift in.  `enable` tells the circuit to shift in the `new_lsb`, and `out` tells us the current value in the register.

```scala
  val shiftReg = RegInit(0.U(reg_len.W))  // where the value gets stored
```

Here, we declare our first sequential element.  We bind `shiftReg` to a Register initialized to a value of zero that is as long as parameterized.  If we don't want to initialize a Register to a certain value in Chisel, we can simply bind to a `Reg()`.

```scala
  when (io.enable){
    val nextShiftReg = Cat(shiftReg(reg_len - 1, 0), io.new_lsb)  // concatenate the old shift reg with the new lsb, minus its msb
    shiftReg        := nextShiftReg  // on every cycle, push in the new value
  }
```

Now, this is the sequential block.  One first thing to note is that, by default, in Chisel, the clock is implicit, generally.  So this block is triggered when the io enable is high and on the rising edge of the implicit clock signal.  When that occurs, we take all but the most significant bit in the internal register, and glue the new least significant bit to its end.  Similar to a left shift, then shift in a new bit.  If enable were low, that internal register would simply hold its value across the clock edge.

```scala
  io.out := shiftReg
}
```

This is pretty cut and dry.  Just pushing the output signal and finishing the module.

```scala
object ShiftRegisterVerilog extends App {
  chisel3.Driver.execute(args, () => new ShiftRegister(16))
}
```

Now this might look a little exotic.  In scala the `object` keyword indicates that we have a singleton object.  This one extends a scala `App` which I won't go into detail here.  Internally, we make a chisel driver to create a 16 bit Shift Register, which allows us to make firrtl and verilog from the chisel.
