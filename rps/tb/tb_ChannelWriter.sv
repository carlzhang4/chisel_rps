`timescale 1ns / 1ns
module tb_ChannelWriter(

    );

reg                 clock                         =0;
reg                 reset                         =0;
reg                 io_aw_ready                   =0;
wire                io_aw_valid                   ;
wire      [32:0]    io_aw_bits_addr               ;
wire      [1:0]     io_aw_bits_burst              ;
wire      [3:0]     io_aw_bits_cache              ;
wire      [5:0]     io_aw_bits_id                 ;
wire      [3:0]     io_aw_bits_len                ;
wire                io_aw_bits_lock               ;
wire      [2:0]     io_aw_bits_prot               ;
wire      [3:0]     io_aw_bits_qos                ;
wire      [3:0]     io_aw_bits_region             ;
wire      [2:0]     io_aw_bits_size               ;
reg                 io_w_ready                    =0;
wire                io_w_valid                    ;
wire      [255:0]   io_w_bits_data                ;
wire                io_w_bits_last                ;
wire      [31:0]    io_w_bits_strb                ;
wire                io_recv_meta_ready            ;
reg                 io_recv_meta_valid            =0;
reg       [15:0]    io_recv_meta_bits_qpn         =0;
reg       [23:0]    io_recv_meta_bits_msg_num     =0;
reg       [20:0]    io_recv_meta_bits_pkg_num     =0;
reg       [20:0]    io_recv_meta_bits_pkg_total   =0;
wire                io_recv_data_ready            ;
reg                 io_recv_data_valid            =0;
reg                 io_recv_data_bits_last        =0;
reg       [511:0]   io_recv_data_bits_data        =0;
reg       [63:0]    io_recv_data_bits_keep        =0;

OUT#(64)out_io_aw(
        clock,
        reset,
        {io_aw_bits_addr,io_aw_bits_burst,io_aw_bits_cache,io_aw_bits_id,io_aw_bits_len,io_aw_bits_lock,io_aw_bits_prot,io_aw_bits_qos,io_aw_bits_region,io_aw_bits_size},
        io_aw_valid,
        io_aw_ready
);
// addr, burst, cache, id, len, lock, prot, qos, region, size
// 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

OUT#(289)out_io_w(
        clock,
        reset,
        {io_w_bits_data,io_w_bits_last,io_w_bits_strb},
        io_w_valid,
        io_w_ready
);
// data, last, strb
// 256'h0, 1'h0, 32'h0

IN#(82)in_io_recv_meta(
        clock,
        reset,
        {io_recv_meta_bits_qpn,io_recv_meta_bits_msg_num,io_recv_meta_bits_pkg_num,io_recv_meta_bits_pkg_total},
        io_recv_meta_valid,
        io_recv_meta_ready
);
// qpn, msg_num, pkg_num, pkg_total
// 16'h0, 24'h0, 21'h0, 21'h0

IN#(577)in_io_recv_data(
        clock,
        reset,
        {io_recv_data_bits_last,io_recv_data_bits_data,io_recv_data_bits_keep},
        io_recv_data_valid,
        io_recv_data_ready
);
// last, data, keep
// 1'h0, 512'h0, 64'h0


ChannelWriter ChannelWriter_inst(
        .*
);


initial begin
        reset <= 1;
        clock = 1;
        #1000;
        reset <= 0;
        #100;
        out_io_aw.start();
        out_io_w.start();
        #50;

		//basic
        in_io_recv_meta.write({16'h0, 24'h0, 21'h0, 21'h0});
		in_io_recv_data.write_many({1'h0, 512'h0, 64'h0}, 63);
		in_io_recv_data.write_many({1'h1, 512'h0, 64'h0}, 1);

		//two consecutive command
		#1000;
		in_io_recv_meta.write({16'h0, 24'h0, 21'h0, 21'h0});
		in_io_recv_meta.write({16'h0, 24'h0, 21'h0, 21'h0});
		in_io_recv_data.write_many({1'h0, 512'h1, 64'h0}, 63);
		in_io_recv_data.write_many({1'h1, 512'h1, 64'h0}, 1);
		in_io_recv_data.write_many({1'h0, 512'h2, 64'h0}, 63);
		in_io_recv_data.write_many({1'h1, 512'h2, 64'h0}, 1);

end
always #5 clock=~clock;

endmodule