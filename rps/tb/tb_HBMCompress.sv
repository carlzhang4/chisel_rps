`timescale 1ns / 1ps

module tb_HBMCompress(

    );

reg                 clock                         =0;
reg                 reset                         =0;
reg                 io_axi_0_aw_ready             =0;
wire                io_axi_0_aw_valid             ;
wire      [32:0]    io_axi_0_aw_bits_addr         ;
wire      [1:0]     io_axi_0_aw_bits_burst        ;
wire      [3:0]     io_axi_0_aw_bits_cache        ;
wire      [5:0]     io_axi_0_aw_bits_id           ;
wire      [3:0]     io_axi_0_aw_bits_len          ;
wire                io_axi_0_aw_bits_lock         ;
wire      [2:0]     io_axi_0_aw_bits_prot         ;
wire      [3:0]     io_axi_0_aw_bits_qos          ;
wire      [3:0]     io_axi_0_aw_bits_region       ;
wire      [2:0]     io_axi_0_aw_bits_size         ;
reg                 io_axi_0_ar_ready             =0;
wire                io_axi_0_ar_valid             ;
wire      [32:0]    io_axi_0_ar_bits_addr         ;
wire      [1:0]     io_axi_0_ar_bits_burst        ;
wire      [3:0]     io_axi_0_ar_bits_cache        ;
wire      [5:0]     io_axi_0_ar_bits_id           ;
wire      [3:0]     io_axi_0_ar_bits_len          ;
wire                io_axi_0_ar_bits_lock         ;
wire      [2:0]     io_axi_0_ar_bits_prot         ;
wire      [3:0]     io_axi_0_ar_bits_qos          ;
wire      [3:0]     io_axi_0_ar_bits_region       ;
wire      [2:0]     io_axi_0_ar_bits_size         ;
reg                 io_axi_0_w_ready              =0;
wire                io_axi_0_w_valid              ;
wire      [255:0]   io_axi_0_w_bits_data          ;
wire                io_axi_0_w_bits_last          ;
wire      [31:0]    io_axi_0_w_bits_strb          ;
wire                io_axi_0_r_ready              ;
reg                 io_axi_0_r_valid              =0;
reg       [255:0]   io_axi_0_r_bits_data          =0;
reg                 io_axi_0_r_bits_last          =0;
reg       [1:0]     io_axi_0_r_bits_resp          =0;
reg       [5:0]     io_axi_0_r_bits_id            =0;
wire                io_axi_0_b_ready              ;
reg                 io_axi_0_b_valid              =0;
reg       [5:0]     io_axi_0_b_bits_id            =0;
reg       [1:0]     io_axi_0_b_bits_resp          =0;
reg                 io_axi_1_aw_ready             =0;
wire                io_axi_1_aw_valid             ;
wire      [32:0]    io_axi_1_aw_bits_addr         ;
wire      [1:0]     io_axi_1_aw_bits_burst        ;
wire      [3:0]     io_axi_1_aw_bits_cache        ;
wire      [5:0]     io_axi_1_aw_bits_id           ;
wire      [3:0]     io_axi_1_aw_bits_len          ;
wire                io_axi_1_aw_bits_lock         ;
wire      [2:0]     io_axi_1_aw_bits_prot         ;
wire      [3:0]     io_axi_1_aw_bits_qos          ;
wire      [3:0]     io_axi_1_aw_bits_region       ;
wire      [2:0]     io_axi_1_aw_bits_size         ;
reg                 io_axi_1_ar_ready             =0;
wire                io_axi_1_ar_valid             ;
wire      [32:0]    io_axi_1_ar_bits_addr         ;
wire      [1:0]     io_axi_1_ar_bits_burst        ;
wire      [3:0]     io_axi_1_ar_bits_cache        ;
wire      [5:0]     io_axi_1_ar_bits_id           ;
wire      [3:0]     io_axi_1_ar_bits_len          ;
wire                io_axi_1_ar_bits_lock         ;
wire      [2:0]     io_axi_1_ar_bits_prot         ;
wire      [3:0]     io_axi_1_ar_bits_qos          ;
wire      [3:0]     io_axi_1_ar_bits_region       ;
wire      [2:0]     io_axi_1_ar_bits_size         ;
reg                 io_axi_1_w_ready              =0;
wire                io_axi_1_w_valid              ;
wire      [255:0]   io_axi_1_w_bits_data          ;
wire                io_axi_1_w_bits_last          ;
wire      [31:0]    io_axi_1_w_bits_strb          ;
wire                io_axi_1_r_ready              ;
reg                 io_axi_1_r_valid              =0;
reg       [255:0]   io_axi_1_r_bits_data          =0;
reg                 io_axi_1_r_bits_last          =0;
reg       [1:0]     io_axi_1_r_bits_resp          =0;
reg       [5:0]     io_axi_1_r_bits_id            =0;
wire                io_axi_1_b_ready              ;
reg                 io_axi_1_b_valid              =0;
reg       [5:0]     io_axi_1_b_bits_id            =0;
reg       [1:0]     io_axi_1_b_bits_resp          =0;
reg                 io_axi_2_aw_ready             =0;
wire                io_axi_2_aw_valid             ;
wire      [32:0]    io_axi_2_aw_bits_addr         ;
wire      [1:0]     io_axi_2_aw_bits_burst        ;
wire      [3:0]     io_axi_2_aw_bits_cache        ;
wire      [5:0]     io_axi_2_aw_bits_id           ;
wire      [3:0]     io_axi_2_aw_bits_len          ;
wire                io_axi_2_aw_bits_lock         ;
wire      [2:0]     io_axi_2_aw_bits_prot         ;
wire      [3:0]     io_axi_2_aw_bits_qos          ;
wire      [3:0]     io_axi_2_aw_bits_region       ;
wire      [2:0]     io_axi_2_aw_bits_size         ;
reg                 io_axi_2_ar_ready             =0;
wire                io_axi_2_ar_valid             ;
wire      [32:0]    io_axi_2_ar_bits_addr         ;
wire      [1:0]     io_axi_2_ar_bits_burst        ;
wire      [3:0]     io_axi_2_ar_bits_cache        ;
wire      [5:0]     io_axi_2_ar_bits_id           ;
wire      [3:0]     io_axi_2_ar_bits_len          ;
wire                io_axi_2_ar_bits_lock         ;
wire      [2:0]     io_axi_2_ar_bits_prot         ;
wire      [3:0]     io_axi_2_ar_bits_qos          ;
wire      [3:0]     io_axi_2_ar_bits_region       ;
wire      [2:0]     io_axi_2_ar_bits_size         ;
reg                 io_axi_2_w_ready              =0;
wire                io_axi_2_w_valid              ;
wire      [255:0]   io_axi_2_w_bits_data          ;
wire                io_axi_2_w_bits_last          ;
wire      [31:0]    io_axi_2_w_bits_strb          ;
wire                io_axi_2_r_ready              ;
reg                 io_axi_2_r_valid              =0;
reg       [255:0]   io_axi_2_r_bits_data          =0;
reg                 io_axi_2_r_bits_last          =0;
reg       [1:0]     io_axi_2_r_bits_resp          =0;
reg       [5:0]     io_axi_2_r_bits_id            =0;
wire                io_axi_2_b_ready              ;
reg                 io_axi_2_b_valid              =0;
reg       [5:0]     io_axi_2_b_bits_id            =0;
reg       [1:0]     io_axi_2_b_bits_resp          =0;
reg                 io_axi_3_aw_ready             =0;
wire                io_axi_3_aw_valid             ;
wire      [32:0]    io_axi_3_aw_bits_addr         ;
wire      [1:0]     io_axi_3_aw_bits_burst        ;
wire      [3:0]     io_axi_3_aw_bits_cache        ;
wire      [5:0]     io_axi_3_aw_bits_id           ;
wire      [3:0]     io_axi_3_aw_bits_len          ;
wire                io_axi_3_aw_bits_lock         ;
wire      [2:0]     io_axi_3_aw_bits_prot         ;
wire      [3:0]     io_axi_3_aw_bits_qos          ;
wire      [3:0]     io_axi_3_aw_bits_region       ;
wire      [2:0]     io_axi_3_aw_bits_size         ;
reg                 io_axi_3_ar_ready             =0;
wire                io_axi_3_ar_valid             ;
wire      [32:0]    io_axi_3_ar_bits_addr         ;
wire      [1:0]     io_axi_3_ar_bits_burst        ;
wire      [3:0]     io_axi_3_ar_bits_cache        ;
wire      [5:0]     io_axi_3_ar_bits_id           ;
wire      [3:0]     io_axi_3_ar_bits_len          ;
wire                io_axi_3_ar_bits_lock         ;
wire      [2:0]     io_axi_3_ar_bits_prot         ;
wire      [3:0]     io_axi_3_ar_bits_qos          ;
wire      [3:0]     io_axi_3_ar_bits_region       ;
wire      [2:0]     io_axi_3_ar_bits_size         ;
reg                 io_axi_3_w_ready              =0;
wire                io_axi_3_w_valid              ;
wire      [255:0]   io_axi_3_w_bits_data          ;
wire                io_axi_3_w_bits_last          ;
wire      [31:0]    io_axi_3_w_bits_strb          ;
wire                io_axi_3_r_ready              ;
reg                 io_axi_3_r_valid              =0;
reg       [255:0]   io_axi_3_r_bits_data          =0;
reg                 io_axi_3_r_bits_last          =0;
reg       [1:0]     io_axi_3_r_bits_resp          =0;
reg       [5:0]     io_axi_3_r_bits_id            =0;
wire                io_axi_3_b_ready              ;
reg                 io_axi_3_b_valid              =0;
reg       [5:0]     io_axi_3_b_bits_id            =0;
reg       [1:0]     io_axi_3_b_bits_resp          =0;
reg                 io_start                      =0;
reg                 io_start_compress             =0;
reg       [31:0]    io_total_cmds                 =0;

OUT#(64)out_io_axi_0_aw(
        clock,
        reset,
        {io_axi_0_aw_bits_addr,io_axi_0_aw_bits_burst,io_axi_0_aw_bits_cache,io_axi_0_aw_bits_id,io_axi_0_aw_bits_len,io_axi_0_aw_bits_lock,io_axi_0_aw_bits_prot,io_axi_0_aw_bits_qos,io_axi_0_aw_bits_region,io_axi_0_aw_bits_size},
        io_axi_0_aw_valid,
        io_axi_0_aw_ready
);
// addr, burst, cache, id, len, lock, prot, qos, region, size
// 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

OUT#(64)out_io_axi_0_ar(
        clock,
        reset,
        {io_axi_0_ar_bits_addr,io_axi_0_ar_bits_burst,io_axi_0_ar_bits_cache,io_axi_0_ar_bits_id,io_axi_0_ar_bits_len,io_axi_0_ar_bits_lock,io_axi_0_ar_bits_prot,io_axi_0_ar_bits_qos,io_axi_0_ar_bits_region,io_axi_0_ar_bits_size},
        io_axi_0_ar_valid,
        io_axi_0_ar_ready
);
// addr, burst, cache, id, len, lock, prot, qos, region, size
// 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

OUT#(289)out_io_axi_0_w(
        clock,
        reset,
        {io_axi_0_w_bits_data,io_axi_0_w_bits_last,io_axi_0_w_bits_strb},
        io_axi_0_w_valid,
        io_axi_0_w_ready
);
// data, last, strb
// 256'h0, 1'h0, 32'h0

IN#(265)in_io_axi_0_r(
        clock,
        reset,
        {io_axi_0_r_bits_data,io_axi_0_r_bits_last,io_axi_0_r_bits_resp,io_axi_0_r_bits_id},
        io_axi_0_r_valid,
        io_axi_0_r_ready
);
// data, last, resp, id
// 256'h0, 1'h0, 2'h0, 6'h0

IN#(8)in_io_axi_0_b(
        clock,
        reset,
        {io_axi_0_b_bits_id,io_axi_0_b_bits_resp},
        io_axi_0_b_valid,
        io_axi_0_b_ready
);
// id, resp
// 6'h0, 2'h0

OUT#(64)out_io_axi_1_aw(
        clock,
        reset,
        {io_axi_1_aw_bits_addr,io_axi_1_aw_bits_burst,io_axi_1_aw_bits_cache,io_axi_1_aw_bits_id,io_axi_1_aw_bits_len,io_axi_1_aw_bits_lock,io_axi_1_aw_bits_prot,io_axi_1_aw_bits_qos,io_axi_1_aw_bits_region,io_axi_1_aw_bits_size},
        io_axi_1_aw_valid,
        io_axi_1_aw_ready
);
// addr, burst, cache, id, len, lock, prot, qos, region, size
// 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

OUT#(64)out_io_axi_1_ar(
        clock,
        reset,
        {io_axi_1_ar_bits_addr,io_axi_1_ar_bits_burst,io_axi_1_ar_bits_cache,io_axi_1_ar_bits_id,io_axi_1_ar_bits_len,io_axi_1_ar_bits_lock,io_axi_1_ar_bits_prot,io_axi_1_ar_bits_qos,io_axi_1_ar_bits_region,io_axi_1_ar_bits_size},
        io_axi_1_ar_valid,
        io_axi_1_ar_ready
);
// addr, burst, cache, id, len, lock, prot, qos, region, size
// 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

OUT#(289)out_io_axi_1_w(
        clock,
        reset,
        {io_axi_1_w_bits_data,io_axi_1_w_bits_last,io_axi_1_w_bits_strb},
        io_axi_1_w_valid,
        io_axi_1_w_ready
);
// data, last, strb
// 256'h0, 1'h0, 32'h0

IN#(265)in_io_axi_1_r(
        clock,
        reset,
        {io_axi_1_r_bits_data,io_axi_1_r_bits_last,io_axi_1_r_bits_resp,io_axi_1_r_bits_id},
        io_axi_1_r_valid,
        io_axi_1_r_ready
);
// data, last, resp, id
// 256'h0, 1'h0, 2'h0, 6'h0

IN#(8)in_io_axi_1_b(
        clock,
        reset,
        {io_axi_1_b_bits_id,io_axi_1_b_bits_resp},
        io_axi_1_b_valid,
        io_axi_1_b_ready
);
// id, resp
// 6'h0, 2'h0

OUT#(64)out_io_axi_2_aw(
        clock,
        reset,
        {io_axi_2_aw_bits_addr,io_axi_2_aw_bits_burst,io_axi_2_aw_bits_cache,io_axi_2_aw_bits_id,io_axi_2_aw_bits_len,io_axi_2_aw_bits_lock,io_axi_2_aw_bits_prot,io_axi_2_aw_bits_qos,io_axi_2_aw_bits_region,io_axi_2_aw_bits_size},
        io_axi_2_aw_valid,
        io_axi_2_aw_ready
);
// addr, burst, cache, id, len, lock, prot, qos, region, size
// 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

OUT#(64)out_io_axi_2_ar(
        clock,
        reset,
        {io_axi_2_ar_bits_addr,io_axi_2_ar_bits_burst,io_axi_2_ar_bits_cache,io_axi_2_ar_bits_id,io_axi_2_ar_bits_len,io_axi_2_ar_bits_lock,io_axi_2_ar_bits_prot,io_axi_2_ar_bits_qos,io_axi_2_ar_bits_region,io_axi_2_ar_bits_size},
        io_axi_2_ar_valid,
        io_axi_2_ar_ready
);
// addr, burst, cache, id, len, lock, prot, qos, region, size
// 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

OUT#(289)out_io_axi_2_w(
        clock,
        reset,
        {io_axi_2_w_bits_data,io_axi_2_w_bits_last,io_axi_2_w_bits_strb},
        io_axi_2_w_valid,
        io_axi_2_w_ready
);
// data, last, strb
// 256'h0, 1'h0, 32'h0

IN#(265)in_io_axi_2_r(
        clock,
        reset,
        {io_axi_2_r_bits_data,io_axi_2_r_bits_last,io_axi_2_r_bits_resp,io_axi_2_r_bits_id},
        io_axi_2_r_valid,
        io_axi_2_r_ready
);
// data, last, resp, id
// 256'h0, 1'h0, 2'h0, 6'h0

IN#(8)in_io_axi_2_b(
        clock,
        reset,
        {io_axi_2_b_bits_id,io_axi_2_b_bits_resp},
        io_axi_2_b_valid,
        io_axi_2_b_ready
);
// id, resp
// 6'h0, 2'h0

OUT#(64)out_io_axi_3_aw(
        clock,
        reset,
        {io_axi_3_aw_bits_addr,io_axi_3_aw_bits_burst,io_axi_3_aw_bits_cache,io_axi_3_aw_bits_id,io_axi_3_aw_bits_len,io_axi_3_aw_bits_lock,io_axi_3_aw_bits_prot,io_axi_3_aw_bits_qos,io_axi_3_aw_bits_region,io_axi_3_aw_bits_size},
        io_axi_3_aw_valid,
        io_axi_3_aw_ready
);
// addr, burst, cache, id, len, lock, prot, qos, region, size
// 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

OUT#(64)out_io_axi_3_ar(
        clock,
        reset,
        {io_axi_3_ar_bits_addr,io_axi_3_ar_bits_burst,io_axi_3_ar_bits_cache,io_axi_3_ar_bits_id,io_axi_3_ar_bits_len,io_axi_3_ar_bits_lock,io_axi_3_ar_bits_prot,io_axi_3_ar_bits_qos,io_axi_3_ar_bits_region,io_axi_3_ar_bits_size},
        io_axi_3_ar_valid,
        io_axi_3_ar_ready
);
// addr, burst, cache, id, len, lock, prot, qos, region, size
// 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

OUT#(289)out_io_axi_3_w(
        clock,
        reset,
        {io_axi_3_w_bits_data,io_axi_3_w_bits_last,io_axi_3_w_bits_strb},
        io_axi_3_w_valid,
        io_axi_3_w_ready
);
// data, last, strb
// 256'h0, 1'h0, 32'h0

IN#(265)in_io_axi_3_r(
        clock,
        reset,
        {io_axi_3_r_bits_data,io_axi_3_r_bits_last,io_axi_3_r_bits_resp,io_axi_3_r_bits_id},
        io_axi_3_r_valid,
        io_axi_3_r_ready
);
// data, last, resp, id
// 256'h0, 1'h0, 2'h0, 6'h0

IN#(8)in_io_axi_3_b(
        clock,
        reset,
        {io_axi_3_b_bits_id,io_axi_3_b_bits_resp},
        io_axi_3_b_valid,
        io_axi_3_b_ready
);
// id, resp
// 6'h0, 2'h0

HBMCompress HBMCompress_inst(
        .*
);


initial begin
        reset <= 1;
        clock = 1;
        #1000;
        reset <= 0;
		io_total_cmds		<= 32'h0004_0000;
        #100;
		out_io_axi_0_aw.start();
		out_io_axi_0_w.start();

		out_io_axi_0_ar.start();
		out_io_axi_1_ar.start();
		out_io_axi_2_ar.start();
		out_io_axi_3_ar.start();
        #50;
		// io_start <= 1;
		// #13000;
		io_start_compress	<= 1;
		in_io_axi_0_r.write_many({256'h0, 1'h0, 2'h0, 6'h0}, 2048);
		in_io_axi_1_r.write_many({256'h1, 1'h0, 2'h0, 6'h0}, 2048);
		in_io_axi_2_r.write_many({256'h2, 1'h0, 2'h0, 6'h0}, 2048);
		in_io_axi_3_r.write_many({256'h3, 1'h0, 2'h0, 6'h0}, 2048);

		in_io_axi_0_r.write_many({256'h0, 1'h0, 2'h0, 6'h0}, 2048);
		in_io_axi_1_r.write_many({256'h1, 1'h0, 2'h0, 6'h0}, 2048);
		in_io_axi_2_r.write_many({256'h2, 1'h0, 2'h0, 6'h0}, 2048);
		in_io_axi_3_r.write_many({256'h3, 1'h0, 2'h0, 6'h0}, 2048);

		in_io_axi_0_r.write_many({256'h0, 1'h0, 2'h0, 6'h0}, 2048);
		in_io_axi_1_r.write_many({256'h1, 1'h0, 2'h0, 6'h0}, 2048);
		in_io_axi_2_r.write_many({256'h2, 1'h0, 2'h0, 6'h0}, 2048);
		in_io_axi_3_r.write_many({256'h3, 1'h0, 2'h0, 6'h0}, 2048);

		in_io_axi_0_r.write_many({256'h0, 1'h0, 2'h0, 6'h0}, 2048);
		in_io_axi_1_r.write_many({256'h1, 1'h0, 2'h0, 6'h0}, 2048);
		in_io_axi_2_r.write_many({256'h2, 1'h0, 2'h0, 6'h0}, 2048);
		in_io_axi_3_r.write_many({256'h3, 1'h0, 2'h0, 6'h0}, 2048);

		// #120_000;
		// io_start_compress	<= 0;
		// #10;
		// io_start_compress	<= 1;
		// in_io_axi_0_r.write_many({256'h0, 1'h0, 2'h0, 6'h0}, 2048);
		// in_io_axi_1_r.write_many({256'h1, 1'h0, 2'h0, 6'h0}, 2048);
		// in_io_axi_2_r.write_many({256'h2, 1'h0, 2'h0, 6'h0}, 2048);
		// in_io_axi_3_r.write_many({256'h3, 1'h0, 2'h0, 6'h0}, 2048);

end
always #5 clock=~clock;

endmodule