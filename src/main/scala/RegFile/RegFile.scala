
package RegFile

import chisel3._
import chisel3.util._

// Read and Write ports 
// IO for the Register files
// params inheritted from instantiated module
class RegFileReadPort (val addr_sz: Int,
                       val data_sz: Int) extends Bundle {

  val addr = Input(UInt(addr_sz.W))
  val data = Output(UInt(data_sz.W))

  override def cloneType = (new RegFileReadPort(addr_sz, data_sz)).asInstanceOf[this.type]
}

class RegFileWritePort (val addr_sz: Int,
                        val data_sz: Int) extends Bundle {

  val addr = Input(UInt(addr_sz.W))
  val data = Input(UInt(data_sz.W))

  override def cloneType = (new RegFileReadPort(addr_sz, data_sz)).asInstanceOf[this.type]
}

// Base class to derive other RFs from
abstract class AbsRegisterFile (reg_count:   Int,
                                data_sz:     Int,
                                read_ports:  Int,
                                write_ports: Int,
                                rv_zero:     Boolean = true) extends Module {

  val rf_addr_sz = log2Ceil(reg_count)

  val io = IO(new Bundle{
    val read  = Vec(read_ports,  new RegFileReadPort (rf_addr_sz, data_sz))
    val write = Vec(write_ports, new RegFileWritePort(rf_addr_sz, data_sz))
  })

  val zero_str = if (rv_zero) " RISC-V style Register Zero" else " Register Zero fully General"
  val infoString = """ ********* Register File Composition *********
                     | Register Count        :   $reg_count
                     | Register Width        :   $data_sz
                     | Number of Read Ports  :   $read_ports
                     | Number of Write Ports :   $write_ports""".stripMargin 

  override def toString: String = infoString + zero_str
}

// Doing this to dry run testing
class RegisterFileBasic (reg_count:   Int,
                         data_sz:     Int,
                         read_ports:  Int,
                         write_ports: Int,
                         rv_zero:     Boolean = true) extends AbsRegisterFile (reg_count, data_sz, read_ports, write_ports, rv_zero) {

// N.B.: IO inherited from abstract [io.read & io.write + implicits]

  val rf = Mem(reg_count, UInt(data_sz.W))

// manage reads, RV0 if needed
  if (rv_zero){
    for(i <- 0 until read_ports)     io.read(i).data := Mux(io.read(i).addr === 0.U, 0.U, rf(io.read(i).addr))
  }
  else for(i <- 0 until read_ports)  io.read(i).data := rf(io.read(i).addr)

// manage writes.  Could block to $r0 if rv_zero to be super safe, but read stuff should handle it
  for(i <- 0 until write_ports) rf(io.write(i).addr) := io.write(i).data
}
