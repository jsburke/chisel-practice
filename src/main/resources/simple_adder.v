
module simple_adder(in, out);

input  [127:0] in;
output [63:0]  out;

out = in[127:64] + in[63:0];

endmodule
