# A Basic Adder in Chisel

This directory contains the Chisel HDL to make a Ripple-Carry Adder from individual Full Adders.  It is meant as an absolute starting point for learning Chisel.

## How to Use

The corresponding test code can be found in [src/test/scala/Adder/AdderUnitTest.scala](../../../test/scala/Adder).  SBT is used to run the tests either by passing arguments to sbt:

```sh
sbt 'testOnly Adder.AdderTester'
```

or by starting sbt and then issuing the test:

```sh
sbt
> testOnly Adder.AdderTester
```

The second is advantageous if you are modifying either the test or the design and then running again because sbt will not have to be kicked off multiple times, which will save you a little time.

At present, when this test is launched it will run several different styles of tests and show the help options.  Some of these will produce a .vcd file that can be viewed with gtkwave.  These files will be in the directories under the `test_run_dir` directory produced at the top level of this project.

## Chisel Explanation

The following is a detailed overview of what is happening in the chisel source.  Since Chisel is built into Scala, there will be a lot of reference to what Scala is doing with links as needed.  To start, Scala is an Object Oriented programming language that works on the JVM that can make use of functional programming paradigms, though it is not strict in its enforcement of functional style like Haskell.  The option to not remain strictly functional is up to the programmer.  For some basic reading on both topics:

- [Object Oriented Programming](https://en.wikipedia.org/wiki/Object-oriented_programming)
- [Functional Programming](https://en.wikipedia.org/wiki/Functional_programming)

Within these tutorials, Object Orientation will arise first and functional programming later.

### Inside the Chisel

Before kicking anything off, it is important to separate two elements of chisel that are easily confused: nodes and data-types. Nodes are chisel objects that represent real circuit elements and data-types are types of values that a particular node may carry.  Nodes are things like Regs, Mems, and so on.  The types on them are the likes of Bool, UInt, Bundles, and Vecs.  Thus, in chisel, when we write `Input(UInt(5.W))` we are saying whatever is bound to this is an input node which is a UInt type, 5 bits wide.  Hopefully this clarifies a bit of potential confusion

Now, open `Adder.scala` in some text editor.

The first lines we will see are

```scala
package Adder

import chisel3._
```

The first line declares that we are designing in a Scala Package called Adder.  All of the classes and objects that are in this file will be a part of the Adder package.

The next line, `import chisel3._`, will be ubiquitous in chisel projects.  It tells scala to import the chisel3 package and everything under it, including objects, classes, and other subpackages.  The underscore character is a wildcard similar to * in Python or Java.  This gives us access to things like chisel's Module, Bundle, and types like Bool and UInt.

After a few comments we come to the first real circuit, a Full Adder:

```scala
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

```

The first line declares a new object class, `FullAdder`, that is a subclass of chisel's Module object which let's us design circuitry. The design, logically, is contained in the brackets that follow it.

The next line starts the input-output declaration for the module.  By declaring it as a Scala `val` we tell Scala that the io is immutable.  In normal scala programming `val pi = 3.14` binds the identifier pi to the float value of 3.14, and it cannot be later altered.  Values that need to be altered in Scala are instead declared by `var`; however the mutability goes against the functional paradigm.  In this case, we bind io to a chisel IO object, which is an object mandatory for all current chisel Modules. The name bound to IO is normally io, much like i,j,k is used for loops in programming in general.  The type of this particular IO is a Bundle, and the scala `new` keyword is used to create a new instance of chisel's Bundle.

Chisel Bundles are aggregate types like structs in Verilog or C.  In our io Bundle, we have five elements which are largely similar. a, b, and cin are all inputs of type UInt, or unsigned integer, one bit wide as shown by the 1.W.  The `1.W` can also be expressed as `width = 1`.  sum and cout are similar, but bound as outputs.

The next two lines are akin to assign statements in Verilog.  First, we need to use `io.sum` instead of just `sum` because sum is part of the Bundle named io.  Next, the `:=` operator wires a signal to a node.  For the first one, we XOR all three of the inputs to get the sum bit.  For the carry out bit, we OR the ANDs of each input.  The basic operators that can be found in chisel are as follows

| Symbol | Operation        | Example |
|--------|------------------|---------|
|        | Bitwise Ops      |         |
|--------|------------------|---------|
| ~      | Invert bits      | ~io.a   |
| &      | AND              | a & b   |
| ^      | XOR              | a ^ a   |
|--------| Reduction        |---------|
| a.andR | AND a's bits     | a.andR  |
| a.orR  | OR a's bits      | a.orR   |
| a.xorR | XOR a's bits     | a.xorR  |
|--------| Arithmetic       |---------|
| +      | add              | a + b   |
| +&     | width expand add | a +& b  |
| -      | subtract         | a - b   |
| -&     | width expand sub | a -& b  |
| *      | multiply         | a * b   |
| /      | divide           | a / b   |
| %      | modulo           | a % b   |
|--------| Logical          |---------|
| >      | greater than     | a > b   |
| <      | less than        | a < b   |
| >=     | greater or equal | a >= b  |
| <=     | lesser or equal  | a <= b  |
| !      | logical not      | !sleep  |
| &&     | logical and      |go && val|
| ===    |        equal     | a === b |
| =/=    | not equal        | a =/=b  |

The OR based operators are based on `|` but I was having trouble with markdown.

Continuing onto the next module:

```scala
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

```

Again, we have a class declared as a subclass of Chisel's Module.  However, this time the class comes with a constructor parameter, num_sz.  It is a regular Scala Int type, not a chisel UInt or SInt.  Declaring it with the `val` keyword makes it a member of the class and exposes it via an acessor.  If the `val` keyword is omitted, its use in the class will determine its resolution as either a private field or never becoming a field and removed by garbage collection.

The IO Bundle this time is a little different.  Two inputs and an output parameterize their width on the constructor parameter.

Following that is a quick print statement that will show when the Adder class is instantiated, but it does not resolve to any RTL.

The next line builds as many Full Adders as bits were declared by the constructor, and binds their io to subAdders.  In general in Scala `Array.fill[Type](num_elements)(value)` is used to create and array of a specific type of a set number of elements and set them to a certain value.  In our case, we make an array of num_sz FullAdder modules, expose their respective io, and then bind that.

The following two use Chisel's Vec type on a Wire.  One is a Vec of one bit UInt, the other Bool, both parameterized by the constructor parameter.

Next, since the lowest order FullAdder needs its cin filled from something other than a lower order FullAdder (since it doesn't exist) we assign the lowest order carry to the input carry in.

Next, we wire FullAdder io up.  The inputs come directly from the Top Level inputs, the carry ins are typically from the carry out of the FullAdder that is one order less than it, except for the one mentioned above.  And the sum bits wire directly to the vector declared in the block after the print.  This is described in a for-loop since the pattern is repetitive, as in first described is the wiring for the lowest order FullAdder, then the next, until we have num_sz FullAdders wired together.  There is no for-loop in the resultant hardware.

After the for-loop is the wiring to the module outputs.  Since subSums is a Vec of Bool and io.sum is a UInt, we cast the Vec to a UInt using the .asUInt method.  The io.cout is wired directly to the cout of the highest order FullAdder.
