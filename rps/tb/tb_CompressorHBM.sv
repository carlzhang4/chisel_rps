`timescale 1ns / 1ps

module tb_CompressorHBM(

    );

reg                 clock                         =0;
reg                 reset                         =0;
reg                 io_hbm_ar_0_ready             =0;
wire                io_hbm_ar_0_valid             ;
wire      [32:0]    io_hbm_ar_0_bits_addr         ;
wire      [1:0]     io_hbm_ar_0_bits_burst        ;
wire      [3:0]     io_hbm_ar_0_bits_cache        ;
wire      [5:0]     io_hbm_ar_0_bits_id           ;
wire      [3:0]     io_hbm_ar_0_bits_len          ;
wire                io_hbm_ar_0_bits_lock         ;
wire      [2:0]     io_hbm_ar_0_bits_prot         ;
wire      [3:0]     io_hbm_ar_0_bits_qos          ;
wire      [3:0]     io_hbm_ar_0_bits_region       ;
wire      [2:0]     io_hbm_ar_0_bits_size         ;
reg                 io_hbm_ar_1_ready             =0;
wire                io_hbm_ar_1_valid             ;
wire      [32:0]    io_hbm_ar_1_bits_addr         ;
wire      [1:0]     io_hbm_ar_1_bits_burst        ;
wire      [3:0]     io_hbm_ar_1_bits_cache        ;
wire      [5:0]     io_hbm_ar_1_bits_id           ;
wire      [3:0]     io_hbm_ar_1_bits_len          ;
wire                io_hbm_ar_1_bits_lock         ;
wire      [2:0]     io_hbm_ar_1_bits_prot         ;
wire      [3:0]     io_hbm_ar_1_bits_qos          ;
wire      [3:0]     io_hbm_ar_1_bits_region       ;
wire      [2:0]     io_hbm_ar_1_bits_size         ;
reg                 io_hbm_ar_2_ready             =0;
wire                io_hbm_ar_2_valid             ;
wire      [32:0]    io_hbm_ar_2_bits_addr         ;
wire      [1:0]     io_hbm_ar_2_bits_burst        ;
wire      [3:0]     io_hbm_ar_2_bits_cache        ;
wire      [5:0]     io_hbm_ar_2_bits_id           ;
wire      [3:0]     io_hbm_ar_2_bits_len          ;
wire                io_hbm_ar_2_bits_lock         ;
wire      [2:0]     io_hbm_ar_2_bits_prot         ;
wire      [3:0]     io_hbm_ar_2_bits_qos          ;
wire      [3:0]     io_hbm_ar_2_bits_region       ;
wire      [2:0]     io_hbm_ar_2_bits_size         ;
reg                 io_hbm_ar_3_ready             =0;
wire                io_hbm_ar_3_valid             ;
wire      [32:0]    io_hbm_ar_3_bits_addr         ;
wire      [1:0]     io_hbm_ar_3_bits_burst        ;
wire      [3:0]     io_hbm_ar_3_bits_cache        ;
wire      [5:0]     io_hbm_ar_3_bits_id           ;
wire      [3:0]     io_hbm_ar_3_bits_len          ;
wire                io_hbm_ar_3_bits_lock         ;
wire      [2:0]     io_hbm_ar_3_bits_prot         ;
wire      [3:0]     io_hbm_ar_3_bits_qos          ;
wire      [3:0]     io_hbm_ar_3_bits_region       ;
wire      [2:0]     io_hbm_ar_3_bits_size         ;
wire                io_hbm_r_0_ready              ;
reg                 io_hbm_r_0_valid              =0;
reg       [255:0]   io_hbm_r_0_bits_data          =0;
reg                 io_hbm_r_0_bits_last          =0;
reg       [1:0]     io_hbm_r_0_bits_resp          =0;
reg       [5:0]     io_hbm_r_0_bits_id            =0;
wire                io_hbm_r_1_ready              ;
reg                 io_hbm_r_1_valid              =0;
reg       [255:0]   io_hbm_r_1_bits_data          =0;
reg                 io_hbm_r_1_bits_last          =0;
reg       [1:0]     io_hbm_r_1_bits_resp          =0;
reg       [5:0]     io_hbm_r_1_bits_id            =0;
wire                io_hbm_r_2_ready              ;
reg                 io_hbm_r_2_valid              =0;
reg       [255:0]   io_hbm_r_2_bits_data          =0;
reg                 io_hbm_r_2_bits_last          =0;
reg       [1:0]     io_hbm_r_2_bits_resp          =0;
reg       [5:0]     io_hbm_r_2_bits_id            =0;
wire                io_hbm_r_3_ready              ;
reg                 io_hbm_r_3_valid              =0;
reg       [255:0]   io_hbm_r_3_bits_data          =0;
reg                 io_hbm_r_3_bits_last          =0;
reg       [1:0]     io_hbm_r_3_bits_resp          =0;
reg       [5:0]     io_hbm_r_3_bits_id            =0;
wire                io_cmd_in_ready               ;
reg                 io_cmd_in_valid               =0;
reg       [63:0]    io_cmd_in_bits_addr           =0;
reg                 io_out_ready                  =0;
wire                io_out_valid                  ;
wire      [511:0]   io_out_bits_data              ;
wire                io_out_bits_last              ;

DMA#(256,10) hbm0(
.clock                         (clock                         ),//input 
.reset                         (reset                         ),//input 
.read_cmd_valid                (io_hbm_ar_0_valid                ),//input 
.read_cmd_ready                (io_hbm_ar_0_ready                ),//output 
.read_cmd_address              (io_hbm_ar_0_bits_addr%(256*1024*1024)              ),//input [63:0]
.read_cmd_length               ((io_hbm_ar_0_bits_len+1)<<5               ),//input [31:0]
.write_cmd_valid               (0               ),//input 
.write_cmd_ready               (               ),//output 
.write_cmd_address             (0             ),//input [63:0]
.write_cmd_length              (0              ),//input [31:0]
.read_data_valid               (io_hbm_r_0_valid               ),//output 
.read_data_ready               (io_hbm_r_0_ready               ),//input 
.read_data_data                (io_hbm_r_0_bits_data                ),//output [width-1:0]
.read_data_keep                (                ),//output [(width/8)-1:0]
.read_data_last                (io_hbm_r_0_bits_last                ),//output 
.write_data_valid              (0              ),//input 
.write_data_ready              (              ),//output 
.write_data_data               (0               ),//input [width-1:0]
.write_data_keep               (0               ),//input [(width/8)-1:0]
.write_data_last               (0               )//input 
);

DMA#(256,10) hbm1(
.clock                         (clock                         ),//input 
.reset                         (reset                         ),//input 
.read_cmd_valid                (io_hbm_ar_1_valid                ),//input 
.read_cmd_ready                (io_hbm_ar_1_ready                ),//output 
.read_cmd_address              (io_hbm_ar_1_bits_addr%(256*1024*1024)              ),//input [63:0]
.read_cmd_length               ((io_hbm_ar_1_bits_len+1)<<5               ),//input [31:0]
.write_cmd_valid               (0               ),//input 
.write_cmd_ready               (               ),//output 
.write_cmd_address             (0             ),//input [63:0]
.write_cmd_length              (0              ),//input [31:0]
.read_data_valid               (io_hbm_r_1_valid               ),//output 
.read_data_ready               (io_hbm_r_1_ready               ),//input 
.read_data_data                (io_hbm_r_1_bits_data                ),//output [width-1:0]
.read_data_keep                (                ),//output [(width/8)-1:0]
.read_data_last                (io_hbm_r_1_bits_last                ),//output 
.write_data_valid              (0              ),//input 
.write_data_ready              (              ),//output 
.write_data_data               (0               ),//input [width-1:0]
.write_data_keep               (0               ),//input [(width/8)-1:0]
.write_data_last               (0               )//input 
);

DMA#(256,10) hbm2(
.clock                         (clock                         ),//input 
.reset                         (reset                         ),//input 
.read_cmd_valid                (io_hbm_ar_2_valid                ),//input 
.read_cmd_ready                (io_hbm_ar_2_ready                ),//output 
.read_cmd_address              (io_hbm_ar_2_bits_addr%(256*1024*1024)              ),//input [63:0]
.read_cmd_length               ((io_hbm_ar_2_bits_len+1)<<5               ),//input [31:0]
.write_cmd_valid               (0               ),//input 
.write_cmd_ready               (               ),//output 
.write_cmd_address             (0             ),//input [63:0]
.write_cmd_length              (0              ),//input [31:0]
.read_data_valid               (io_hbm_r_2_valid               ),//output 
.read_data_ready               (io_hbm_r_2_ready               ),//input 
.read_data_data                (io_hbm_r_2_bits_data                ),//output [width-1:0]
.read_data_keep                (                ),//output [(width/8)-1:0]
.read_data_last                (io_hbm_r_2_bits_last                ),//output 
.write_data_valid              (0              ),//input 
.write_data_ready              (              ),//output 
.write_data_data               (0               ),//input [width-1:0]
.write_data_keep               (0               ),//input [(width/8)-1:0]
.write_data_last               (0               )//input 
);

DMA#(256,10) hbm3(
.clock                         (clock                         ),//input 
.reset                         (reset                         ),//input 
.read_cmd_valid                (io_hbm_ar_3_valid                ),//input 
.read_cmd_ready                (io_hbm_ar_3_ready                ),//output 
.read_cmd_address              (io_hbm_ar_3_bits_addr%(256*1024*1024)              ),//input [63:0]
.read_cmd_length               ((io_hbm_ar_3_bits_len+1)<<5               ),//input [31:0]
.write_cmd_valid               (0               ),//input 
.write_cmd_ready               (               ),//output 
.write_cmd_address             (0             ),//input [63:0]
.write_cmd_length              (0              ),//input [31:0]
.read_data_valid               (io_hbm_r_3_valid               ),//output 
.read_data_ready               (io_hbm_r_3_ready               ),//input 
.read_data_data                (io_hbm_r_3_bits_data                ),//output [width-1:0]
.read_data_keep                (                ),//output [(width/8)-1:0]
.read_data_last                (io_hbm_r_3_bits_last                ),//output 
.write_data_valid              (0              ),//input 
.write_data_ready              (              ),//output 
.write_data_data               (0               ),//input [width-1:0]
.write_data_keep               (0               ),//input [(width/8)-1:0]
.write_data_last               (0               )//input 
);



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


CompressorHBM CompressorHBM_inst(
        .*
);


initial begin
        reset <= 1;
        clock = 1;
        #1000;
        reset <= 0;
        #100;
		hbm0.init_incr_hbm();
		hbm1.init_incr_hbm(1);
		hbm2.init_incr_hbm(2);
		hbm3.init_incr_hbm(3);
        out_io_out.start();
        #50;
		in_io_cmd_in.write({64'h0});
		in_io_cmd_in.write({64'h0});
		in_io_cmd_in.write({64'h0});
		in_io_cmd_in.write({64'h0});
		in_io_cmd_in.write({64'h0});
		in_io_cmd_in.write({256*1024*1024});
		in_io_cmd_in.write({512*1024*1024});
		in_io_cmd_in.write({768*1024*1024});
		in_io_cmd_in.write({64'h0});
		in_io_cmd_in.write({256*1024*1024});
		in_io_cmd_in.write({512*1024*1024});
		in_io_cmd_in.write({768*1024*1024});
		in_io_cmd_in.write({64'h0});
		in_io_cmd_in.write({256*1024*1024});
		in_io_cmd_in.write({512*1024*1024});
		in_io_cmd_in.write({768*1024*1024});
		in_io_cmd_in.write({64'h0});
		in_io_cmd_in.write({256*1024*1024});
		in_io_cmd_in.write({512*1024*1024});
		in_io_cmd_in.write({768*1024*1024});
end
always #5 clock=~clock;

endmodule