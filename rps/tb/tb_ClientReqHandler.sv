`timescale 1ns / 1ns
module tb_ClientReqHandler(

    );

reg                 clock                         =0;
reg                 reset                         =0;
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
reg                 io_axi_hbm_0_aw_ready         =0;
wire                io_axi_hbm_0_aw_valid         ;
wire      [32:0]    io_axi_hbm_0_aw_bits_addr     ;
wire      [1:0]     io_axi_hbm_0_aw_bits_burst    ;
wire      [3:0]     io_axi_hbm_0_aw_bits_cache    ;
wire      [5:0]     io_axi_hbm_0_aw_bits_id       ;
wire      [3:0]     io_axi_hbm_0_aw_bits_len      ;
wire                io_axi_hbm_0_aw_bits_lock     ;
wire      [2:0]     io_axi_hbm_0_aw_bits_prot     ;
wire      [3:0]     io_axi_hbm_0_aw_bits_qos      ;
wire      [3:0]     io_axi_hbm_0_aw_bits_region   ;
wire      [2:0]     io_axi_hbm_0_aw_bits_size     ;
reg                 io_axi_hbm_0_ar_ready         =0;
wire                io_axi_hbm_0_ar_valid         ;
wire      [32:0]    io_axi_hbm_0_ar_bits_addr     ;
wire      [1:0]     io_axi_hbm_0_ar_bits_burst    ;
wire      [3:0]     io_axi_hbm_0_ar_bits_cache    ;
wire      [5:0]     io_axi_hbm_0_ar_bits_id       ;
wire      [3:0]     io_axi_hbm_0_ar_bits_len      ;
wire                io_axi_hbm_0_ar_bits_lock     ;
wire      [2:0]     io_axi_hbm_0_ar_bits_prot     ;
wire      [3:0]     io_axi_hbm_0_ar_bits_qos      ;
wire      [3:0]     io_axi_hbm_0_ar_bits_region   ;
wire      [2:0]     io_axi_hbm_0_ar_bits_size     ;
reg                 io_axi_hbm_0_w_ready          =0;
wire                io_axi_hbm_0_w_valid          ;
wire      [255:0]   io_axi_hbm_0_w_bits_data      ;
wire                io_axi_hbm_0_w_bits_last      ;
wire      [31:0]    io_axi_hbm_0_w_bits_strb      ;
wire                io_axi_hbm_0_r_ready          ;
reg                 io_axi_hbm_0_r_valid          =0;
reg       [255:0]   io_axi_hbm_0_r_bits_data      =0;
reg                 io_axi_hbm_0_r_bits_last      =0;
reg       [1:0]     io_axi_hbm_0_r_bits_resp      =0;
reg       [5:0]     io_axi_hbm_0_r_bits_id        =0;
wire                io_axi_hbm_0_b_ready          ;
reg                 io_axi_hbm_0_b_valid          =0;
reg       [5:0]     io_axi_hbm_0_b_bits_id        =0;
reg       [1:0]     io_axi_hbm_0_b_bits_resp      =0;
reg                 io_axi_hbm_1_aw_ready         =0;
wire                io_axi_hbm_1_aw_valid         ;
wire      [32:0]    io_axi_hbm_1_aw_bits_addr     ;
wire      [1:0]     io_axi_hbm_1_aw_bits_burst    ;
wire      [3:0]     io_axi_hbm_1_aw_bits_cache    ;
wire      [5:0]     io_axi_hbm_1_aw_bits_id       ;
wire      [3:0]     io_axi_hbm_1_aw_bits_len      ;
wire                io_axi_hbm_1_aw_bits_lock     ;
wire      [2:0]     io_axi_hbm_1_aw_bits_prot     ;
wire      [3:0]     io_axi_hbm_1_aw_bits_qos      ;
wire      [3:0]     io_axi_hbm_1_aw_bits_region   ;
wire      [2:0]     io_axi_hbm_1_aw_bits_size     ;
reg                 io_axi_hbm_1_ar_ready         =0;
wire                io_axi_hbm_1_ar_valid         ;
wire      [32:0]    io_axi_hbm_1_ar_bits_addr     ;
wire      [1:0]     io_axi_hbm_1_ar_bits_burst    ;
wire      [3:0]     io_axi_hbm_1_ar_bits_cache    ;
wire      [5:0]     io_axi_hbm_1_ar_bits_id       ;
wire      [3:0]     io_axi_hbm_1_ar_bits_len      ;
wire                io_axi_hbm_1_ar_bits_lock     ;
wire      [2:0]     io_axi_hbm_1_ar_bits_prot     ;
wire      [3:0]     io_axi_hbm_1_ar_bits_qos      ;
wire      [3:0]     io_axi_hbm_1_ar_bits_region   ;
wire      [2:0]     io_axi_hbm_1_ar_bits_size     ;
reg                 io_axi_hbm_1_w_ready          =0;
wire                io_axi_hbm_1_w_valid          ;
wire      [255:0]   io_axi_hbm_1_w_bits_data      ;
wire                io_axi_hbm_1_w_bits_last      ;
wire      [31:0]    io_axi_hbm_1_w_bits_strb      ;
wire                io_axi_hbm_1_r_ready          ;
reg                 io_axi_hbm_1_r_valid          =0;
reg       [255:0]   io_axi_hbm_1_r_bits_data      =0;
reg                 io_axi_hbm_1_r_bits_last      =0;
reg       [1:0]     io_axi_hbm_1_r_bits_resp      =0;
reg       [5:0]     io_axi_hbm_1_r_bits_id        =0;
wire                io_axi_hbm_1_b_ready          ;
reg                 io_axi_hbm_1_b_valid          =0;
reg       [5:0]     io_axi_hbm_1_b_bits_id        =0;
reg       [1:0]     io_axi_hbm_1_b_bits_resp      =0;
reg                 io_axi_hbm_2_aw_ready         =0;
wire                io_axi_hbm_2_aw_valid         ;
wire      [32:0]    io_axi_hbm_2_aw_bits_addr     ;
wire      [1:0]     io_axi_hbm_2_aw_bits_burst    ;
wire      [3:0]     io_axi_hbm_2_aw_bits_cache    ;
wire      [5:0]     io_axi_hbm_2_aw_bits_id       ;
wire      [3:0]     io_axi_hbm_2_aw_bits_len      ;
wire                io_axi_hbm_2_aw_bits_lock     ;
wire      [2:0]     io_axi_hbm_2_aw_bits_prot     ;
wire      [3:0]     io_axi_hbm_2_aw_bits_qos      ;
wire      [3:0]     io_axi_hbm_2_aw_bits_region   ;
wire      [2:0]     io_axi_hbm_2_aw_bits_size     ;
reg                 io_axi_hbm_2_ar_ready         =0;
wire                io_axi_hbm_2_ar_valid         ;
wire      [32:0]    io_axi_hbm_2_ar_bits_addr     ;
wire      [1:0]     io_axi_hbm_2_ar_bits_burst    ;
wire      [3:0]     io_axi_hbm_2_ar_bits_cache    ;
wire      [5:0]     io_axi_hbm_2_ar_bits_id       ;
wire      [3:0]     io_axi_hbm_2_ar_bits_len      ;
wire                io_axi_hbm_2_ar_bits_lock     ;
wire      [2:0]     io_axi_hbm_2_ar_bits_prot     ;
wire      [3:0]     io_axi_hbm_2_ar_bits_qos      ;
wire      [3:0]     io_axi_hbm_2_ar_bits_region   ;
wire      [2:0]     io_axi_hbm_2_ar_bits_size     ;
reg                 io_axi_hbm_2_w_ready          =0;
wire                io_axi_hbm_2_w_valid          ;
wire      [255:0]   io_axi_hbm_2_w_bits_data      ;
wire                io_axi_hbm_2_w_bits_last      ;
wire      [31:0]    io_axi_hbm_2_w_bits_strb      ;
wire                io_axi_hbm_2_r_ready          ;
reg                 io_axi_hbm_2_r_valid          =0;
reg       [255:0]   io_axi_hbm_2_r_bits_data      =0;
reg                 io_axi_hbm_2_r_bits_last      =0;
reg       [1:0]     io_axi_hbm_2_r_bits_resp      =0;
reg       [5:0]     io_axi_hbm_2_r_bits_id        =0;
wire                io_axi_hbm_2_b_ready          ;
reg                 io_axi_hbm_2_b_valid          =0;
reg       [5:0]     io_axi_hbm_2_b_bits_id        =0;
reg       [1:0]     io_axi_hbm_2_b_bits_resp      =0;
reg                 io_axi_hbm_3_aw_ready         =0;
wire                io_axi_hbm_3_aw_valid         ;
wire      [32:0]    io_axi_hbm_3_aw_bits_addr     ;
wire      [1:0]     io_axi_hbm_3_aw_bits_burst    ;
wire      [3:0]     io_axi_hbm_3_aw_bits_cache    ;
wire      [5:0]     io_axi_hbm_3_aw_bits_id       ;
wire      [3:0]     io_axi_hbm_3_aw_bits_len      ;
wire                io_axi_hbm_3_aw_bits_lock     ;
wire      [2:0]     io_axi_hbm_3_aw_bits_prot     ;
wire      [3:0]     io_axi_hbm_3_aw_bits_qos      ;
wire      [3:0]     io_axi_hbm_3_aw_bits_region   ;
wire      [2:0]     io_axi_hbm_3_aw_bits_size     ;
reg                 io_axi_hbm_3_ar_ready         =0;
wire                io_axi_hbm_3_ar_valid         ;
wire      [32:0]    io_axi_hbm_3_ar_bits_addr     ;
wire      [1:0]     io_axi_hbm_3_ar_bits_burst    ;
wire      [3:0]     io_axi_hbm_3_ar_bits_cache    ;
wire      [5:0]     io_axi_hbm_3_ar_bits_id       ;
wire      [3:0]     io_axi_hbm_3_ar_bits_len      ;
wire                io_axi_hbm_3_ar_bits_lock     ;
wire      [2:0]     io_axi_hbm_3_ar_bits_prot     ;
wire      [3:0]     io_axi_hbm_3_ar_bits_qos      ;
wire      [3:0]     io_axi_hbm_3_ar_bits_region   ;
wire      [2:0]     io_axi_hbm_3_ar_bits_size     ;
reg                 io_axi_hbm_3_w_ready          =0;
wire                io_axi_hbm_3_w_valid          ;
wire      [255:0]   io_axi_hbm_3_w_bits_data      ;
wire                io_axi_hbm_3_w_bits_last      ;
wire      [31:0]    io_axi_hbm_3_w_bits_strb      ;
wire                io_axi_hbm_3_r_ready          ;
reg                 io_axi_hbm_3_r_valid          =0;
reg       [255:0]   io_axi_hbm_3_r_bits_data      =0;
reg                 io_axi_hbm_3_r_bits_last      =0;
reg       [1:0]     io_axi_hbm_3_r_bits_resp      =0;
reg       [5:0]     io_axi_hbm_3_r_bits_id        =0;
wire                io_axi_hbm_3_b_ready          ;
reg                 io_axi_hbm_3_b_valid          =0;
reg       [5:0]     io_axi_hbm_3_b_bits_id        =0;
reg       [1:0]     io_axi_hbm_3_b_bits_resp      =0;
wire                io_meta_from_host_ready       ;
reg                 io_meta_from_host_valid       =0;
reg       [511:0]   io_meta_from_host_bits_data   =0;
reg                 io_meta2host_ready            =0;
wire                io_meta2host_valid            ;
wire      [31:0]    io_meta2host_bits_addr_offset ;
wire      [15:0]    io_meta2host_bits_len         ;
reg                 io_data2host_ready            =0;
wire                io_data2host_valid            ;
wire                io_data2host_bits_last        ;
wire      [511:0]   io_data2host_bits_data        ;
reg                 io_out_meta_ready             =0;
wire                io_out_meta_valid             ;
wire      [1:0]     io_out_meta_bits_rdma_cmd     ;
wire      [23:0]    io_out_meta_bits_qpn          ;
wire      [47:0]    io_out_meta_bits_local_vaddr  ;
wire      [47:0]    io_out_meta_bits_remote_vaddr ;
wire      [31:0]    io_out_meta_bits_length       ;
reg                 io_out_data_ready             =0;
wire                io_out_data_valid             ;
wire                io_out_data_bits_last         ;
wire      [511:0]   io_out_data_bits_data         ;
wire      [63:0]    io_out_data_bits_keep         ;

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

// OUT#(64)out_io_axi_hbm_0_aw(
//         clock,
//         reset,
//         {io_axi_hbm_0_aw_bits_addr,io_axi_hbm_0_aw_bits_burst,io_axi_hbm_0_aw_bits_cache,io_axi_hbm_0_aw_bits_id,io_axi_hbm_0_aw_bits_len,io_axi_hbm_0_aw_bits_lock,io_axi_hbm_0_aw_bits_prot,io_axi_hbm_0_aw_bits_qos,io_axi_hbm_0_aw_bits_region,io_axi_hbm_0_aw_bits_size},
//         io_axi_hbm_0_aw_valid,
//         io_axi_hbm_0_aw_ready
// );
// // addr, burst, cache, id, len, lock, prot, qos, region, size
// // 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

// OUT#(64)out_io_axi_hbm_0_ar(
//         clock,
//         reset,
//         {io_axi_hbm_0_ar_bits_addr,io_axi_hbm_0_ar_bits_burst,io_axi_hbm_0_ar_bits_cache,io_axi_hbm_0_ar_bits_id,io_axi_hbm_0_ar_bits_len,io_axi_hbm_0_ar_bits_lock,io_axi_hbm_0_ar_bits_prot,io_axi_hbm_0_ar_bits_qos,io_axi_hbm_0_ar_bits_region,io_axi_hbm_0_ar_bits_size},
//         io_axi_hbm_0_ar_valid,
//         io_axi_hbm_0_ar_ready
// );
// // addr, burst, cache, id, len, lock, prot, qos, region, size
// // 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

// OUT#(289)out_io_axi_hbm_0_w(
//         clock,
//         reset,
//         {io_axi_hbm_0_w_bits_data,io_axi_hbm_0_w_bits_last,io_axi_hbm_0_w_bits_strb},
//         io_axi_hbm_0_w_valid,
//         io_axi_hbm_0_w_ready
// );
// // data, last, strb
// // 256'h0, 1'h0, 32'h0

// IN#(265)in_io_axi_hbm_0_r(
//         clock,
//         reset,
//         {io_axi_hbm_0_r_bits_data,io_axi_hbm_0_r_bits_last,io_axi_hbm_0_r_bits_resp,io_axi_hbm_0_r_bits_id},
//         io_axi_hbm_0_r_valid,
//         io_axi_hbm_0_r_ready
// );
// // data, last, resp, id
// // 256'h0, 1'h0, 2'h0, 6'h0

// IN#(8)in_io_axi_hbm_0_b(
//         clock,
//         reset,
//         {io_axi_hbm_0_b_bits_id,io_axi_hbm_0_b_bits_resp},
//         io_axi_hbm_0_b_valid,
//         io_axi_hbm_0_b_ready
// );
// // id, resp
// // 6'h0, 2'h0

// OUT#(64)out_io_axi_hbm_1_aw(
//         clock,
//         reset,
//         {io_axi_hbm_1_aw_bits_addr,io_axi_hbm_1_aw_bits_burst,io_axi_hbm_1_aw_bits_cache,io_axi_hbm_1_aw_bits_id,io_axi_hbm_1_aw_bits_len,io_axi_hbm_1_aw_bits_lock,io_axi_hbm_1_aw_bits_prot,io_axi_hbm_1_aw_bits_qos,io_axi_hbm_1_aw_bits_region,io_axi_hbm_1_aw_bits_size},
//         io_axi_hbm_1_aw_valid,
//         io_axi_hbm_1_aw_ready
// );
// // addr, burst, cache, id, len, lock, prot, qos, region, size
// // 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

// OUT#(64)out_io_axi_hbm_1_ar(
//         clock,
//         reset,
//         {io_axi_hbm_1_ar_bits_addr,io_axi_hbm_1_ar_bits_burst,io_axi_hbm_1_ar_bits_cache,io_axi_hbm_1_ar_bits_id,io_axi_hbm_1_ar_bits_len,io_axi_hbm_1_ar_bits_lock,io_axi_hbm_1_ar_bits_prot,io_axi_hbm_1_ar_bits_qos,io_axi_hbm_1_ar_bits_region,io_axi_hbm_1_ar_bits_size},
//         io_axi_hbm_1_ar_valid,
//         io_axi_hbm_1_ar_ready
// );
// // addr, burst, cache, id, len, lock, prot, qos, region, size
// // 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

// OUT#(289)out_io_axi_hbm_1_w(
//         clock,
//         reset,
//         {io_axi_hbm_1_w_bits_data,io_axi_hbm_1_w_bits_last,io_axi_hbm_1_w_bits_strb},
//         io_axi_hbm_1_w_valid,
//         io_axi_hbm_1_w_ready
// );
// // data, last, strb
// // 256'h0, 1'h0, 32'h0

// IN#(265)in_io_axi_hbm_1_r(
//         clock,
//         reset,
//         {io_axi_hbm_1_r_bits_data,io_axi_hbm_1_r_bits_last,io_axi_hbm_1_r_bits_resp,io_axi_hbm_1_r_bits_id},
//         io_axi_hbm_1_r_valid,
//         io_axi_hbm_1_r_ready
// );
// // data, last, resp, id
// // 256'h0, 1'h0, 2'h0, 6'h0

// IN#(8)in_io_axi_hbm_1_b(
//         clock,
//         reset,
//         {io_axi_hbm_1_b_bits_id,io_axi_hbm_1_b_bits_resp},
//         io_axi_hbm_1_b_valid,
//         io_axi_hbm_1_b_ready
// );
// // id, resp
// // 6'h0, 2'h0

// OUT#(64)out_io_axi_hbm_2_aw(
//         clock,
//         reset,
//         {io_axi_hbm_2_aw_bits_addr,io_axi_hbm_2_aw_bits_burst,io_axi_hbm_2_aw_bits_cache,io_axi_hbm_2_aw_bits_id,io_axi_hbm_2_aw_bits_len,io_axi_hbm_2_aw_bits_lock,io_axi_hbm_2_aw_bits_prot,io_axi_hbm_2_aw_bits_qos,io_axi_hbm_2_aw_bits_region,io_axi_hbm_2_aw_bits_size},
//         io_axi_hbm_2_aw_valid,
//         io_axi_hbm_2_aw_ready
// );
// // addr, burst, cache, id, len, lock, prot, qos, region, size
// // 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

// OUT#(64)out_io_axi_hbm_2_ar(
//         clock,
//         reset,
//         {io_axi_hbm_2_ar_bits_addr,io_axi_hbm_2_ar_bits_burst,io_axi_hbm_2_ar_bits_cache,io_axi_hbm_2_ar_bits_id,io_axi_hbm_2_ar_bits_len,io_axi_hbm_2_ar_bits_lock,io_axi_hbm_2_ar_bits_prot,io_axi_hbm_2_ar_bits_qos,io_axi_hbm_2_ar_bits_region,io_axi_hbm_2_ar_bits_size},
//         io_axi_hbm_2_ar_valid,
//         io_axi_hbm_2_ar_ready
// );
// // addr, burst, cache, id, len, lock, prot, qos, region, size
// // 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

// OUT#(289)out_io_axi_hbm_2_w(
//         clock,
//         reset,
//         {io_axi_hbm_2_w_bits_data,io_axi_hbm_2_w_bits_last,io_axi_hbm_2_w_bits_strb},
//         io_axi_hbm_2_w_valid,
//         io_axi_hbm_2_w_ready
// );
// // data, last, strb
// // 256'h0, 1'h0, 32'h0

// IN#(265)in_io_axi_hbm_2_r(
//         clock,
//         reset,
//         {io_axi_hbm_2_r_bits_data,io_axi_hbm_2_r_bits_last,io_axi_hbm_2_r_bits_resp,io_axi_hbm_2_r_bits_id},
//         io_axi_hbm_2_r_valid,
//         io_axi_hbm_2_r_ready
// );
// // data, last, resp, id
// // 256'h0, 1'h0, 2'h0, 6'h0

// IN#(8)in_io_axi_hbm_2_b(
//         clock,
//         reset,
//         {io_axi_hbm_2_b_bits_id,io_axi_hbm_2_b_bits_resp},
//         io_axi_hbm_2_b_valid,
//         io_axi_hbm_2_b_ready
// );
// // id, resp
// // 6'h0, 2'h0

// OUT#(64)out_io_axi_hbm_3_aw(
//         clock,
//         reset,
//         {io_axi_hbm_3_aw_bits_addr,io_axi_hbm_3_aw_bits_burst,io_axi_hbm_3_aw_bits_cache,io_axi_hbm_3_aw_bits_id,io_axi_hbm_3_aw_bits_len,io_axi_hbm_3_aw_bits_lock,io_axi_hbm_3_aw_bits_prot,io_axi_hbm_3_aw_bits_qos,io_axi_hbm_3_aw_bits_region,io_axi_hbm_3_aw_bits_size},
//         io_axi_hbm_3_aw_valid,
//         io_axi_hbm_3_aw_ready
// );
// // addr, burst, cache, id, len, lock, prot, qos, region, size
// // 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

// OUT#(64)out_io_axi_hbm_3_ar(
//         clock,
//         reset,
//         {io_axi_hbm_3_ar_bits_addr,io_axi_hbm_3_ar_bits_burst,io_axi_hbm_3_ar_bits_cache,io_axi_hbm_3_ar_bits_id,io_axi_hbm_3_ar_bits_len,io_axi_hbm_3_ar_bits_lock,io_axi_hbm_3_ar_bits_prot,io_axi_hbm_3_ar_bits_qos,io_axi_hbm_3_ar_bits_region,io_axi_hbm_3_ar_bits_size},
//         io_axi_hbm_3_ar_valid,
//         io_axi_hbm_3_ar_ready
// );
// // addr, burst, cache, id, len, lock, prot, qos, region, size
// // 33'h0, 2'h0, 4'h0, 6'h0, 4'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

// OUT#(289)out_io_axi_hbm_3_w(
//         clock,
//         reset,
//         {io_axi_hbm_3_w_bits_data,io_axi_hbm_3_w_bits_last,io_axi_hbm_3_w_bits_strb},
//         io_axi_hbm_3_w_valid,
//         io_axi_hbm_3_w_ready
// );
// // data, last, strb
// // 256'h0, 1'h0, 32'h0

// IN#(265)in_io_axi_hbm_3_r(
//         clock,
//         reset,
//         {io_axi_hbm_3_r_bits_data,io_axi_hbm_3_r_bits_last,io_axi_hbm_3_r_bits_resp,io_axi_hbm_3_r_bits_id},
//         io_axi_hbm_3_r_valid,
//         io_axi_hbm_3_r_ready
// );
// // data, last, resp, id
// // 256'h0, 1'h0, 2'h0, 6'h0

// IN#(8)in_io_axi_hbm_3_b(
//         clock,
//         reset,
//         {io_axi_hbm_3_b_bits_id,io_axi_hbm_3_b_bits_resp},
//         io_axi_hbm_3_b_valid,
//         io_axi_hbm_3_b_ready
// );
// // id, resp
// // 6'h0, 2'h0

IN#(512)in_io_meta_from_host(
        clock,
        reset,
        {io_meta_from_host_bits_data},
        io_meta_from_host_valid,
        io_meta_from_host_ready
);
// data
// 512'h0

OUT#(48)out_io_meta2host(
        clock,
        reset,
        {io_meta2host_bits_addr_offset,io_meta2host_bits_len},
        io_meta2host_valid,
        io_meta2host_ready
);
// addr_offset, len
// 32'h0, 16'h0

OUT#(513)out_io_data2host(
        clock,
        reset,
        {io_data2host_bits_last,io_data2host_bits_data},
        io_data2host_valid,
        io_data2host_ready
);
// last, data
// 1'h0, 512'h0

OUT#(154)out_io_out_meta(
        clock,
        reset,
        {io_out_meta_bits_rdma_cmd,io_out_meta_bits_qpn,io_out_meta_bits_local_vaddr,io_out_meta_bits_remote_vaddr,io_out_meta_bits_length},
        io_out_meta_valid,
        io_out_meta_ready
);
// rdma_cmd, qpn, local_vaddr, remote_vaddr, length
// 2'h0, 24'h0, 48'h0, 48'h0, 32'h0

OUT#(577)out_io_out_data(
        clock,
        reset,
        {io_out_data_bits_last,io_out_data_bits_data,io_out_data_bits_keep},
        io_out_data_valid,
        io_out_data_ready
);
// last, data, keep
// 1'h0, 512'h0, 64'h0


ClientReqHandler ClientReqHandler_inst(
        .*
);

DMA#(256,10) hbm0(
.clock                         (clock                         ),//input 
.reset                         (reset                         ),//input 
.read_cmd_valid                (io_axi_hbm_0_ar_valid                ),//input 
.read_cmd_ready                (io_axi_hbm_0_ar_ready                ),//output 
.read_cmd_address              (io_axi_hbm_0_ar_bits_addr              ),//input [63:0]
.read_cmd_length               ((io_axi_hbm_0_ar_bits_len+1)<<5               ),//input [31:0]
.write_cmd_valid               (io_axi_hbm_0_aw_valid               ),//input 
.write_cmd_ready               (io_axi_hbm_0_aw_ready               ),//output 
.write_cmd_address             (io_axi_hbm_0_aw_bits_addr             ),//input [63:0]
.write_cmd_length              ((io_axi_hbm_0_aw_bits_len+1)<<5              ),//input [31:0]
.read_data_valid               (io_axi_hbm_0_r_valid               ),//output 
.read_data_ready               (io_axi_hbm_0_r_ready               ),//input 
.read_data_data                (io_axi_hbm_0_r_bits_data                ),//output [width-1:0]
.read_data_keep                (                ),//output [(width/8)-1:0]
.read_data_last                (io_axi_hbm_0_r_bits_last                ),//output 
.write_data_valid              (io_axi_hbm_0_w_valid              ),//input 
.write_data_ready              (io_axi_hbm_0_w_ready              ),//output 
.write_data_data               (io_axi_hbm_0_w_bits_data               ),//input [width-1:0]
.write_data_keep               (io_axi_hbm_0_w_bits_strb               ),//input [(width/8)-1:0]
.write_data_last               (io_axi_hbm_0_w_bits_last               )//input 
);

DMA#(256,10) hbm1(
.clock                         (clock                         ),//input 
.reset                         (reset                         ),//input 
.read_cmd_valid                (io_axi_hbm_1_ar_valid                ),//input 
.read_cmd_ready                (io_axi_hbm_1_ar_ready                ),//output 
.read_cmd_address              (io_axi_hbm_1_ar_bits_addr              ),//input [63:0]
.read_cmd_length               ((io_axi_hbm_1_ar_bits_len+1)<<5               ),//input [31:0]
.write_cmd_valid               (io_axi_hbm_1_aw_valid               ),//input 
.write_cmd_ready               (io_axi_hbm_1_aw_ready               ),//output 
.write_cmd_address             (io_axi_hbm_1_aw_bits_addr             ),//input [63:0]
.write_cmd_length              ((io_axi_hbm_1_aw_bits_len+1)<<5              ),//input [31:0]
.read_data_valid               (io_axi_hbm_1_r_valid               ),//output 
.read_data_ready               (io_axi_hbm_1_r_ready               ),//input 
.read_data_data                (io_axi_hbm_1_r_bits_data                ),//output [width-1:0]
.read_data_keep                (                ),//output [(width/8)-1:0]
.read_data_last                (io_axi_hbm_1_r_bits_last                ),//output 
.write_data_valid              (io_axi_hbm_1_w_valid              ),//input 
.write_data_ready              (io_axi_hbm_1_w_ready              ),//output 
.write_data_data               (io_axi_hbm_1_w_bits_data               ),//input [width-1:0]
.write_data_keep               (io_axi_hbm_1_w_bits_strb               ),//input [(width/8)-1:0]
.write_data_last               (io_axi_hbm_1_w_bits_last               )//input 
);

DMA#(256,10) hbm2(
.clock                         (clock                         ),//input 
.reset                         (reset                         ),//input 
.read_cmd_valid                (io_axi_hbm_2_ar_valid                ),//input 
.read_cmd_ready                (io_axi_hbm_2_ar_ready                ),//output 
.read_cmd_address              (io_axi_hbm_2_ar_bits_addr              ),//input [63:0]
.read_cmd_length               ((io_axi_hbm_2_ar_bits_len+1)<<5               ),//input [31:0]
.write_cmd_valid               (io_axi_hbm_2_aw_valid               ),//input 
.write_cmd_ready               (io_axi_hbm_2_aw_ready               ),//output 
.write_cmd_address             (io_axi_hbm_2_aw_bits_addr             ),//input [63:0]
.write_cmd_length              ((io_axi_hbm_2_aw_bits_len+1)<<5              ),//input [31:0]
.read_data_valid               (io_axi_hbm_2_r_valid               ),//output 
.read_data_ready               (io_axi_hbm_2_r_ready               ),//input 
.read_data_data                (io_axi_hbm_2_r_bits_data                ),//output [width-1:0]
.read_data_keep                (                ),//output [(width/8)-1:0]
.read_data_last                (io_axi_hbm_2_r_bits_last                ),//output 
.write_data_valid              (io_axi_hbm_2_w_valid              ),//input 
.write_data_ready              (io_axi_hbm_2_w_ready              ),//output 
.write_data_data               (io_axi_hbm_2_w_bits_data               ),//input [width-1:0]
.write_data_keep               (io_axi_hbm_2_w_bits_strb               ),//input [(width/8)-1:0]
.write_data_last               (io_axi_hbm_2_w_bits_last               )//input 
);

DMA#(256,10) hbm3(
.clock                         (clock                         ),//input 
.reset                         (reset                         ),//input 
.read_cmd_valid                (io_axi_hbm_3_ar_valid                ),//input 
.read_cmd_ready                (io_axi_hbm_3_ar_ready                ),//output 
.read_cmd_address              (io_axi_hbm_3_ar_bits_addr              ),//input [63:0]
.read_cmd_length               ((io_axi_hbm_3_ar_bits_len+1)<<5               ),//input [31:0]
.write_cmd_valid               (io_axi_hbm_3_aw_valid               ),//input 
.write_cmd_ready               (io_axi_hbm_3_aw_ready               ),//output 
.write_cmd_address             (io_axi_hbm_3_aw_bits_addr             ),//input [63:0]
.write_cmd_length              ((io_axi_hbm_3_aw_bits_len+1)<<5              ),//input [31:0]
.read_data_valid               (io_axi_hbm_3_r_valid               ),//output 
.read_data_ready               (io_axi_hbm_3_r_ready               ),//input 
.read_data_data                (io_axi_hbm_3_r_bits_data                ),//output [width-1:0]
.read_data_keep                (                ),//output [(width/8)-1:0]
.read_data_last                (io_axi_hbm_3_r_bits_last                ),//output 
.write_data_valid              (io_axi_hbm_3_w_valid              ),//input 
.write_data_ready              (io_axi_hbm_3_w_ready              ),//output 
.write_data_data               (io_axi_hbm_3_w_bits_data               ),//input [width-1:0]
.write_data_keep               (io_axi_hbm_3_w_bits_strb               ),//input [(width/8)-1:0]
.write_data_last               (io_axi_hbm_3_w_bits_last               )//input 
);

initial begin
        reset <= 1;
        clock = 1;
        #1000;
        reset <= 0;
        #100;
		out_io_meta2host.start();
		out_io_data2host.start();
		out_io_out_meta.start();
        out_io_out_data.start();

        #50;
		// qpn, msg_num, pkg_num, pkg_total
		in_io_recv_meta.write({16'h1, 24'h0, 21'h0, 21'h0});
		in_io_recv_meta.write({16'h1, 24'h0, 21'h0, 21'h0});
		in_io_recv_meta.write({16'h1, 24'h0, 21'h0, 21'h0});
		in_io_recv_meta.write({16'h1, 24'h0, 21'h0, 21'h0});
		in_io_recv_meta.write({16'h1, 24'h0, 21'h0, 21'h0});
		in_io_recv_meta.write({16'h1, 24'h0, 21'h0, 21'h0});
		in_io_recv_meta.write({16'h1, 24'h0, 21'h0, 21'h0});
		in_io_recv_meta.write({16'h1, 24'h0, 21'h0, 21'h0});
		in_io_recv_data.write_many({1'h0, 512'h0, 64'h0}, 63);
		in_io_recv_data.write_many({1'h1, 512'h0, 64'h0}, 1);
		in_io_recv_data.write_many({1'h0, 512'h1, 64'h0}, 63);
		in_io_recv_data.write_many({1'h1, 512'h1, 64'h0}, 1);
		in_io_recv_data.write_many({1'h0, 512'h2, 64'h0}, 63);
		in_io_recv_data.write_many({1'h1, 512'h2, 64'h0}, 1);
		in_io_recv_data.write_many({1'h0, 512'h3, 64'h0}, 63);
		in_io_recv_data.write_many({1'h1, 512'h3, 64'h0}, 1);
		in_io_recv_data.write_many({1'h0, 512'h4, 64'h0}, 63);
		in_io_recv_data.write_many({1'h1, 512'h4, 64'h0}, 1);
		in_io_recv_data.write_many({1'h0, 512'h5, 64'h0}, 63);
		in_io_recv_data.write_many({1'h1, 512'h5, 64'h0}, 1);
		in_io_recv_data.write_many({1'h0, 512'h6, 64'h0}, 63);
		in_io_recv_data.write_many({1'h1, 512'h6, 64'h0}, 1);
		in_io_recv_data.write_many({1'h0, 512'h7, 64'h0}, 63);
		in_io_recv_data.write_many({1'h1, 512'h7, 64'h0}, 1);

		#10000;
		in_io_meta_from_host.write({512'h0});
		in_io_meta_from_host.write({512'h0});
		in_io_meta_from_host.write({512'h0});
		in_io_meta_from_host.write({512'h0});
		in_io_meta_from_host.write({512'h0});
		in_io_meta_from_host.write({512'h0});
		in_io_meta_from_host.write({512'h0});
		in_io_meta_from_host.write({512'h0});
end
always #5 clock=~clock;

endmodule