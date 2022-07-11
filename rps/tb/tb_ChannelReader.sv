`timescale 1ns / 1ps
module tb_ChannelReader(

    );

reg                 clock                         =0;
reg                 reset                         =0;
reg                 io_ar_ready                   =0;
wire                io_ar_valid                   ;
wire      [32:0]    io_ar_bits_addr               ;
wire      [1:0]     io_ar_bits_burst              ;
wire      [3:0]     io_ar_bits_cache              ;
wire      [5:0]     io_ar_bits_id                 ;
wire      [3:0]     io_ar_bits_len                ;
wire                io_ar_bits_lock               ;
wire      [2:0]     io_ar_bits_prot               ;
wire      [3:0]     io_ar_bits_qos                ;
wire      [3:0]     io_ar_bits_region             ;
wire      [2:0]     io_ar_bits_size               ;
wire                io_r_ready                    ;
reg                 io_r_valid                    =0;
reg       [255:0]   io_r_bits_data                =0;
reg                 io_r_bits_last                =0;
reg       [1:0]     io_r_bits_resp                =0;
reg       [5:0]     io_r_bits_id                  =0;
wire                io_cmd_in_ready               ;
reg                 io_cmd_in_valid               =0;
reg       [63:0]    io_cmd_in_bits_addr           =0;
reg                 io_out_ready                  =0;
wire                io_out_valid                  ;
wire      [511:0]   io_out_bits_data              ;
wire                io_out_bits_last              ;

// OUT#(64)out_io_ar(
//         clock,
//         reset,
//         {io_ar_bits_addr,io_ar_bits_burst,io_ar_bits_cache,io_ar_bits_id,io_ar_bits_len,io_ar_bits_lock,io_ar_bits_prot,io_ar_bits_qos,io_ar_bits_region,io_ar_bits_size},
//         io_ar_valid,
//         io_ar_ready
// );
// // addr, burst, cache, id, len, lock, prot, qos, region, size
// // 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

// IN#(265)in_io_r(
//         clock,
//         reset,
//         {io_r_bits_data,io_r_bits_last,io_r_bits_resp,io_r_bits_id},
//         io_r_valid,
//         io_r_ready
// );
// // data, last, resp, id
// // 256'h0, 1'h0, 2'h0, 6'h0

IN#(64)in_io_cmd_in(
        clock,
        reset,
        {io_cmd_in_bits_addr},
        io_cmd_in_valid,
        io_cmd_in_ready
);
// addr
// 64'h0

OUT#(513)out_io_out(
        clock,
        reset,
        {io_out_bits_data,io_out_bits_last},
        io_out_valid,
        io_out_ready
);
// data, last
// 512'h0, 1'h0


ChannelReader ChannelReader_inst(
        .*
);

DMA#(256,10) hbm0(
.clock                         (clock                         ),//input 
.reset                         (reset                         ),//input 
.read_cmd_valid                (io_ar_valid                ),//input 
.read_cmd_ready                (io_ar_ready                ),//output 
.read_cmd_address              (io_ar_bits_addr              ),//input [63:0]
.read_cmd_length               ((io_ar_bits_len+1)<<5               ),//input [31:0]
.write_cmd_valid               (0               ),//input 
.write_cmd_ready               (               ),//output 
.write_cmd_address             (0             ),//input [63:0]
.write_cmd_length              (0              ),//input [31:0]
.read_data_valid               (io_r_valid               ),//output 
.read_data_ready               (io_r_ready               ),//input 
.read_data_data                (io_r_bits_data                ),//output [width-1:0]
.read_data_keep                (                ),//output [(width/8)-1:0]
.read_data_last                (io_r_bits_last                ),//output 
.write_data_valid              (0              ),//input 
.write_data_ready              (              ),//output 
.write_data_data               (0               ),//input [width-1:0]
.write_data_keep               (0               ),//input [(width/8)-1:0]
.write_data_last               (0               )//input 
);


initial begin
        reset <= 1;
        clock = 1;
        #1000;
        reset <= 0;
        #100;
		hbm0.init_incr();
        out_io_out.start();
        #50;
		in_io_cmd_in.write({64'h0});
		in_io_cmd_in.write({64'h00000});
		in_io_cmd_in.write({64'h00000});
end
always #5 clock=~clock;

endmodule
