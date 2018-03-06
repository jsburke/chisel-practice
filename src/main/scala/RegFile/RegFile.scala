
package RegFile

import chisel3._
import chisel3.util._

class RegFileEnq (val width:       Int,
                  val read_ports:  Int,
                  val write_ports: Int) extends Bundle {

  val rd_addr = Vec(read_ports,  UInt(width.W))
  val wr_addr = Vec(write_ports, UInt(width.W))
  val wr_data = Vec(write_ports, UInt(widht.W))

  override def cloneType = (new RegFileIn(width, read_ports, write_ports)).asInstanceOf[this.type]
}
