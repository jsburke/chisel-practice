
package RegFile

import chisel3._
import chisel3.util._  // to use chisel's log2Up() function

import scala.collection.breakOut

////////////////////////////////////////////////////////////////
//
// RegFile: Basic RISC-V style Register File with write bypass
//          Not really good for CPU usage
//          Introduce Chisel Mem
//          Introduce some Scala style programming
//
////////////////////////////////////////////////////////////////

class RegFileIO (val no_rd_ports:  Int, 
                 val no_wrt_ports: Int,
                 val addr_bits:    Int,
                 val reg_sz:       Int) extends Bundle{

  val rd_addrs   = Input(Vec(no_rd_ports, UInt(addr_bits.W)))
  val rd_datas   = Output(Vec(no_rd_ports, UInt(reg_sz.W)))

  val wrt_addrs  = Input(Vec(no_wrt_ports, UInt(addr_bits.W)))
  val wrt_datas  = Input(Vec(no_wrt_ports, UInt(reg_sz.W)))
  val wrt_valids = Input(Vec(no_wrt_ports, Bool()))
}

class RegFile (val no_rd_ports:  Int,
               val no_wrt_ports: Int,
               val no_regs:      Int,
               val reg_sz:       Int) extends Module{

  val io = IO(new RegFileIO(no_rd_ports, no_wrt_ports, log2Up(no_regs), reg_sz))
  
  // Declare the Register File as a Mem
  val regFile = Mem(no_regs, Bits(reg_sz.W))

  // Handle the write ports
  for(i <- 0 until no_wrt_ports){
    regFile(io.wrt_addrs(i)) := Mux(io.wrt_valids(i), io.wrt_datas(i),  regFile(io.wrt_addrs(i)))    
  }

  val wrt_map = (io.wrt_addrs zip io.wrt_datas zip io.wrt_valids)(breakOut) // map to tuples [(port, data, valid), ...]
//  def rf_read(rd_port: UInt, rf:
}
