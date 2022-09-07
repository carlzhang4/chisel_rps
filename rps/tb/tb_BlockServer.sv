`timescale 1ns / 1ns
module tb_BlockServer(

    );

reg                 clock                         =0;
reg                 reset                         =0;
wire                io_axib_aw_ready              ;
reg                 io_axib_aw_valid              =0;
reg       [63:0]    io_axib_aw_bits_addr          =0;
reg       [1:0]     io_axib_aw_bits_burst         =0;
reg       [3:0]     io_axib_aw_bits_cache         =0;
reg       [3:0]     io_axib_aw_bits_id            =0;
reg       [7:0]     io_axib_aw_bits_len           =0;
reg                 io_axib_aw_bits_lock          =0;
reg       [2:0]     io_axib_aw_bits_prot          =0;
reg       [3:0]     io_axib_aw_bits_qos           =0;
reg       [3:0]     io_axib_aw_bits_region        =0;
reg       [2:0]     io_axib_aw_bits_size          =0;
wire                io_axib_ar_ready              ;
reg                 io_axib_ar_valid              =0;
reg       [63:0]    io_axib_ar_bits_addr          =0;
reg       [1:0]     io_axib_ar_bits_burst         =0;
reg       [3:0]     io_axib_ar_bits_cache         =0;
reg       [3:0]     io_axib_ar_bits_id            =0;
reg       [7:0]     io_axib_ar_bits_len           =0;
reg                 io_axib_ar_bits_lock          =0;
reg       [2:0]     io_axib_ar_bits_prot          =0;
reg       [3:0]     io_axib_ar_bits_qos           =0;
reg       [3:0]     io_axib_ar_bits_region        =0;
reg       [2:0]     io_axib_ar_bits_size          =0;
wire                io_axib_w_ready               ;
reg                 io_axib_w_valid               =0;
reg       [511:0]   io_axib_w_bits_data           =0;
reg                 io_axib_w_bits_last           =0;
reg       [63:0]    io_axib_w_bits_strb           =0;
reg                 io_axib_r_ready               =0;
wire                io_axib_r_valid               ;
wire      [511:0]   io_axib_r_bits_data           ;
wire                io_axib_r_bits_last           ;
wire      [1:0]     io_axib_r_bits_resp           ;
wire      [3:0]     io_axib_r_bits_id             ;
reg                 io_axib_b_ready               =0;
wire                io_axib_b_valid               ;
wire      [3:0]     io_axib_b_bits_id             ;
wire      [1:0]     io_axib_b_bits_resp           ;
reg       [63:0]    io_start_addr                 =0;
reg       [31:0]    io_num_rpcs                   =0;
reg       [31:0]    io_pfch_tag                   =0;
reg       [31:0]    io_tag_index                  =0;
reg                 io_c2h_cmd_ready              =0;
wire                io_c2h_cmd_valid              ;
wire      [63:0]    io_c2h_cmd_bits_addr          ;
wire      [10:0]    io_c2h_cmd_bits_qid           ;
wire                io_c2h_cmd_bits_error         ;
wire      [7:0]     io_c2h_cmd_bits_func          ;
wire      [2:0]     io_c2h_cmd_bits_port_id       ;
wire      [6:0]     io_c2h_cmd_bits_pfch_tag      ;
wire      [31:0]    io_c2h_cmd_bits_len           ;
reg                 io_c2h_data_ready             =0;
wire                io_c2h_data_valid             ;
wire      [511:0]   io_c2h_data_bits_data         ;
wire      [31:0]    io_c2h_data_bits_tcrc         ;
wire                io_c2h_data_bits_ctrl_marker  ;
wire      [6:0]     io_c2h_data_bits_ctrl_ecc     ;
wire      [31:0]    io_c2h_data_bits_ctrl_len     ;
wire      [2:0]     io_c2h_data_bits_ctrl_port_id ;
wire      [10:0]    io_c2h_data_bits_ctrl_qid     ;
wire                io_c2h_data_bits_ctrl_has_cmpt;
wire                io_c2h_data_bits_last         ;
wire      [5:0]     io_c2h_data_bits_mty          ;
reg                 io_h2c_cmd_ready              =0;
wire                io_h2c_cmd_valid              ;
wire      [63:0]    io_h2c_cmd_bits_addr          ;
wire      [31:0]    io_h2c_cmd_bits_len           ;
wire                io_h2c_cmd_bits_eop           ;
wire                io_h2c_cmd_bits_sop           ;
wire                io_h2c_cmd_bits_mrkr_req      ;
wire                io_h2c_cmd_bits_sdi           ;
wire      [10:0]    io_h2c_cmd_bits_qid           ;
wire                io_h2c_cmd_bits_error         ;
wire      [7:0]     io_h2c_cmd_bits_func          ;
wire      [15:0]    io_h2c_cmd_bits_cidx          ;
wire      [2:0]     io_h2c_cmd_bits_port_id       ;
wire                io_h2c_cmd_bits_no_dma        ;
wire                io_h2c_data_ready             ;
reg                 io_h2c_data_valid             =0;
reg       [511:0]   io_h2c_data_bits_data         =0;
reg       [31:0]    io_h2c_data_bits_tcrc         =0;
reg       [10:0]    io_h2c_data_bits_tuser_qid    =0;
reg       [2:0]     io_h2c_data_bits_tuser_port_id=0;
reg                 io_h2c_data_bits_tuser_err    =0;
reg       [31:0]    io_h2c_data_bits_tuser_mdata  =0;
reg       [5:0]     io_h2c_data_bits_tuser_mty    =0;
reg                 io_h2c_data_bits_tuser_zero_byte=0;
reg                 io_h2c_data_bits_last         =0;
reg                 io_send_meta_ready            =0;
wire                io_send_meta_valid            ;
wire      [1:0]     io_send_meta_bits_rdma_cmd    ;
wire      [23:0]    io_send_meta_bits_qpn         ;
wire      [47:0]    io_send_meta_bits_local_vaddr ;
wire      [47:0]    io_send_meta_bits_remote_vaddr;
wire      [31:0]    io_send_meta_bits_length      ;
reg                 io_send_data_ready            =0;
wire                io_send_data_valid            ;
wire                io_send_data_bits_last        ;
wire      [511:0]   io_send_data_bits_data        ;
wire      [63:0]    io_send_data_bits_keep        ;
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

IN#(97)in_io_axib_aw(
        clock,
        reset,
        {io_axib_aw_bits_addr,io_axib_aw_bits_burst,io_axib_aw_bits_cache,io_axib_aw_bits_id,io_axib_aw_bits_len,io_axib_aw_bits_lock,io_axib_aw_bits_prot,io_axib_aw_bits_qos,io_axib_aw_bits_region,io_axib_aw_bits_size},
        io_axib_aw_valid,
        io_axib_aw_ready
);
// addr, burst, cache, id, len, lock, prot, qos, region, size
// 64'h0, 2'h0, 4'h0, 4'h0, 8'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

IN#(97)in_io_axib_ar(
        clock,
        reset,
        {io_axib_ar_bits_addr,io_axib_ar_bits_burst,io_axib_ar_bits_cache,io_axib_ar_bits_id,io_axib_ar_bits_len,io_axib_ar_bits_lock,io_axib_ar_bits_prot,io_axib_ar_bits_qos,io_axib_ar_bits_region,io_axib_ar_bits_size},
        io_axib_ar_valid,
        io_axib_ar_ready
);
// addr, burst, cache, id, len, lock, prot, qos, region, size
// 64'h0, 2'h0, 4'h0, 4'h0, 8'h0, 1'h0, 3'h0, 4'h0, 4'h0, 3'h0

IN#(577)in_io_axib_w(
        clock,
        reset,
        {io_axib_w_bits_data,io_axib_w_bits_last,io_axib_w_bits_strb},
        io_axib_w_valid,
        io_axib_w_ready
);
// data, last, strb
// 512'h0, 1'h0, 64'h0

OUT#(519)out_io_axib_r(
        clock,
        reset,
        {io_axib_r_bits_data,io_axib_r_bits_last,io_axib_r_bits_resp,io_axib_r_bits_id},
        io_axib_r_valid,
        io_axib_r_ready
);
// data, last, resp, id
// 512'h0, 1'h0, 2'h0, 4'h0

OUT#(6)out_io_axib_b(
        clock,
        reset,
        {io_axib_b_bits_id,io_axib_b_bits_resp},
        io_axib_b_valid,
        io_axib_b_ready
);
// id, resp
// 4'h0, 2'h0

OUT#(126)out_io_c2h_cmd(
        clock,
        reset,
        {io_c2h_cmd_bits_addr,io_c2h_cmd_bits_qid,io_c2h_cmd_bits_error,io_c2h_cmd_bits_func,io_c2h_cmd_bits_port_id,io_c2h_cmd_bits_pfch_tag,io_c2h_cmd_bits_len},
        io_c2h_cmd_valid,
        io_c2h_cmd_ready
);
// addr, qid, error, func, port_id, pfch_tag, len
// 64'h0, 11'h0, 1'h0, 8'h0, 3'h0, 7'h0, 32'h0

OUT#(606)out_io_c2h_data(
        clock,
        reset,
        {io_c2h_data_bits_data,io_c2h_data_bits_tcrc,io_c2h_data_bits_ctrl_marker,io_c2h_data_bits_ctrl_ecc,io_c2h_data_bits_ctrl_len,io_c2h_data_bits_ctrl_port_id,io_c2h_data_bits_ctrl_qid,io_c2h_data_bits_ctrl_has_cmpt,io_c2h_data_bits_last,io_c2h_data_bits_mty},
        io_c2h_data_valid,
        io_c2h_data_ready
);
// data, tcrc, ctrl_marker, ctrl_ecc, ctrl_len, ctrl_port_id, ctrl_qid, ctrl_has_cmpt, last, mty
// 512'h0, 32'h0, 1'h0, 7'h0, 32'h0, 3'h0, 11'h0, 1'h0, 1'h0, 6'h0

OUT#(140)out_io_h2c_cmd(
        clock,
        reset,
        {io_h2c_cmd_bits_addr,io_h2c_cmd_bits_len,io_h2c_cmd_bits_eop,io_h2c_cmd_bits_sop,io_h2c_cmd_bits_mrkr_req,io_h2c_cmd_bits_sdi,io_h2c_cmd_bits_qid,io_h2c_cmd_bits_error,io_h2c_cmd_bits_func,io_h2c_cmd_bits_cidx,io_h2c_cmd_bits_port_id,io_h2c_cmd_bits_no_dma},
        io_h2c_cmd_valid,
        io_h2c_cmd_ready
);
// addr, len, eop, sop, mrkr_req, sdi, qid, error, func, cidx, port_id, no_dma
// 64'h0, 32'h0, 1'h0, 1'h0, 1'h0, 1'h0, 11'h0, 1'h0, 8'h0, 16'h0, 3'h0, 1'h0

IN#(599)in_io_h2c_data(
        clock,
        reset,
        {io_h2c_data_bits_data,io_h2c_data_bits_tcrc,io_h2c_data_bits_tuser_qid,io_h2c_data_bits_tuser_port_id,io_h2c_data_bits_tuser_err,io_h2c_data_bits_tuser_mdata,io_h2c_data_bits_tuser_mty,io_h2c_data_bits_tuser_zero_byte,io_h2c_data_bits_last},
        io_h2c_data_valid,
        io_h2c_data_ready
);
// data, tcrc, tuser_qid, tuser_port_id, tuser_err, tuser_mdata, tuser_mty, tuser_zero_byte, last
// 512'h0, 32'h0, 11'h0, 3'h0, 1'h0, 32'h0, 6'h0, 1'h0, 1'h0

OUT#(154)out_io_send_meta(
        clock,
        reset,
        {io_send_meta_bits_rdma_cmd,io_send_meta_bits_qpn,io_send_meta_bits_local_vaddr,io_send_meta_bits_remote_vaddr,io_send_meta_bits_length},
        io_send_meta_valid,
        io_send_meta_ready
);
// rdma_cmd, qpn, local_vaddr, remote_vaddr, length
// 2'h0, 24'h0, 48'h0, 48'h0, 32'h0

OUT#(577)out_io_send_data(
        clock,
        reset,
        {io_send_data_bits_last,io_send_data_bits_data,io_send_data_bits_keep},
        io_send_data_valid,
        io_send_data_ready
);
// last, data, keep
// 1'h0, 512'h0, 64'h0

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


BlockServer BlockServer_inst(
        .*
);

/*
addr,burst,cache,id,len,lock,prot,qos,region,size
in_io_axib_aw.write({64'h0,2'h0,4'h0,4'h0,8'h0,1'h0,3'h0,4'h0,4'h0,3'h0});

addr,burst,cache,id,len,lock,prot,qos,region,size
in_io_axib_ar.write({64'h0,2'h0,4'h0,4'h0,8'h0,1'h0,3'h0,4'h0,4'h0,3'h0});

data,last,strb
in_io_axib_w.write({512'h0,1'h0,64'h0});

data,tcrc,tuser_qid,tuser_port_id,tuser_err,tuser_mdata,tuser_mty,tuser_zero_byte,last
in_io_h2c_data.write({512'h0,32'h0,11'h0,3'h0,1'h0,32'h0,6'h0,1'h0,1'h0});

qpn,msg_num,pkg_num,pkg_total
in_io_recv_meta.write({16'h0,24'h0,21'h0,21'h0});

last,data,keep
in_io_recv_data.write({1'h0,512'h0,64'h0});

*/

initial begin
        reset <= 1;
        clock = 1;
        #1000;
        reset <= 0;
        #100;
        out_io_axib_r.start();
        out_io_axib_b.start();
        out_io_c2h_cmd.start();
        // out_io_c2h_data.start();
        out_io_h2c_cmd.start();
        out_io_send_meta.start();
        out_io_send_data.start();
        #50;
		in_io_recv_meta.write({16'h1,24'h0,21'h0,21'h0});
		
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h0,512'h0,64'h0});
		in_io_recv_data.write({1'h1,512'h0,64'h0});
end

always #5 clock=~clock;
endmodule