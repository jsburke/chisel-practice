
package RegFile

import chisel3._
import chisel3.util._


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

abstract class AbsRegisterFile (val reg_count:   Int,
                                val rv_zero:     Boolean, // register zero always zero?
                                val data_sz:     Int,
                                val read_ports:  Int,
                                val write_ports: Int) extends Module {

  val rd_port_addr_sz = log2Ceil(read_ports)
  val wr_port_addr_sz = log2Ceil(write_ports)

  val io = IO(new Bundle{
    val read  = Vec(read_ports,  new RegFileReadPort (rd_port_addr_sz, data_sz))
    val write = Vec(write_ports, new RegFileWritePort(wr_port_addr_sz, data_sz))
  })

  val zero_str = if (rv_zero) " RISC-V style Register Zero" else " Register Zero fully General"
  val infoString = """ ********* Register File Composition *********
                     | Register Count        :   $reg_count
                     | Register Width        :   $data_sz
                     | Number of Read Ports  :   $read_ports
                     | Number of Write Ports :   $write_ports""".stripMargin 

  override def toString: String = infoString + zero_str
}
