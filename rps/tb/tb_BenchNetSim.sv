`timescale 1ns / 1ns
module tb_BenchNetSim(

    );

reg                 clock                         =0;
reg                 reset                         =0;
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
reg       [31:0]    io_start                      =0;
wire      [31:0]    io_counters_0                 ;
wire      [31:0]    io_counters_1                 ;
wire      [31:0]    io_counters_2                 ;
wire      [31:0]    io_counters_3                 ;
wire      [31:0]    io_counters_4                 ;
wire      [31:0]    io_counters_5                 ;
wire      [31:0]    io_counters_6                 ;
wire      [31:0]    io_counters_7                 ;
wire      [31:0]    io_counters_8                 ;
wire      [31:0]    io_counters_9                 ;
wire      [31:0]    io_counters_10                ;
wire      [31:0]    io_counters_11                ;
wire      [31:0]    io_counters_12                ;
wire      [31:0]    io_counters_13                ;
wire      [31:0]    io_counters_14                ;
wire      [31:0]    io_counters_15                ;
wire      [31:0]    io_counters_16                ;
wire      [31:0]    io_counters_17                ;
wire      [31:0]    io_counters_18                ;
wire      [31:0]    io_counters_19                ;
wire      [31:0]    io_counters_20                ;
wire      [31:0]    io_counters_21                ;
wire      [31:0]    io_counters_22                ;
wire      [31:0]    io_counters_23                ;
wire      [31:0]    io_counters_24                ;
wire      [31:0]    io_counters_25                ;
wire      [31:0]    io_counters_26                ;
wire      [31:0]    io_counters_27                ;
wire      [31:0]    io_counters_28                ;
wire      [31:0]    io_counters_29                ;
wire      [31:0]    io_counters_30                ;
wire      [31:0]    io_counters_31                ;
wire      [31:0]    io_counters_32                ;
wire      [31:0]    io_counters_33                ;
wire      [31:0]    io_counters_34                ;
wire      [31:0]    io_counters_35                ;
wire      [31:0]    io_counters_36                ;
wire      [31:0]    io_counters_37                ;
wire      [31:0]    io_counters_38                ;
wire      [31:0]    io_counters_39                ;
wire      [31:0]    io_counters_40                ;
wire      [31:0]    io_counters_41                ;
wire      [31:0]    io_counters_42                ;
wire      [31:0]    io_counters_43                ;
wire      [31:0]    io_counters_44                ;
wire      [31:0]    io_counters_45                ;
wire      [31:0]    io_counters_46                ;
wire      [31:0]    io_counters_47                ;
wire      [31:0]    io_counters_48                ;
wire      [31:0]    io_counters_49                ;
wire      [31:0]    io_counters_50                ;
wire      [31:0]    io_counters_51                ;
wire      [31:0]    io_counters_52                ;
wire      [31:0]    io_counters_53                ;
wire      [31:0]    io_counters_54                ;
wire      [31:0]    io_counters_55                ;
wire      [31:0]    io_counters_56                ;
wire      [31:0]    io_counters_57                ;
wire      [31:0]    io_counters_58                ;
wire      [31:0]    io_counters_59                ;
wire      [31:0]    io_counters_60                ;
wire      [31:0]    io_counters_61                ;
wire      [31:0]    io_counters_62                ;
wire      [31:0]    io_counters_63                ;
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


BenchNetSim BenchNetSim_inst(
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

/*
data,last,resp,id
in_io_axi_hbm_0_r.write({256'h0,1'h0,2'h0,6'h0});

id,resp
in_io_axi_hbm_0_b.write({6'h0,2'h0});

data,last,resp,id
in_io_axi_hbm_1_r.write({256'h0,1'h0,2'h0,6'h0});

id,resp
in_io_axi_hbm_1_b.write({6'h0,2'h0});

data,last,resp,id
in_io_axi_hbm_2_r.write({256'h0,1'h0,2'h0,6'h0});

id,resp
in_io_axi_hbm_2_b.write({6'h0,2'h0});

data,last,resp,id
in_io_axi_hbm_3_r.write({256'h0,1'h0,2'h0,6'h0});

id,resp
in_io_axi_hbm_3_b.write({6'h0,2'h0});

addr,burst,cache,id,len,lock,prot,qos,region,size
in_io_axib_aw.write({64'h0,2'h0,4'h0,4'h0,8'h0,1'h0,3'h0,4'h0,4'h0,3'h0});

addr,burst,cache,id,len,lock,prot,qos,region,size
in_io_axib_ar.write({64'h0,2'h0,4'h0,4'h0,8'h0,1'h0,3'h0,4'h0,4'h0,3'h0});

data,last,strb
in_io_axib_w.write({512'h0,1'h0,64'h0});

data,tcrc,tuser_qid,tuser_port_id,tuser_err,tuser_mdata,tuser_mty,tuser_zero_byte,last
in_io_h2c_data.write({512'h0,32'h0,11'h0,3'h0,1'h0,32'h0,6'h0,1'h0,1'h0});

*/

`define HOST_MEM_OFFSET 1024*1024*1024
always @(posedge clock) begin
	if(io_c2h_data_valid & io_c2h_data_ready)begin
		if(io_c2h_data_bits_data >= `HOST_MEM_OFFSET)begin
			in_io_axib_w.write({32'h2,480'h0,1'h0,64'h0});
		end 
		else begin
			in_io_axib_w.write({32'h1,480'h0,1'h0,64'h0});
		end
		
	end
end

initial begin
        reset <= 1;
        clock = 1;
        #1000;
        reset <= 0;
        #100;
        out_io_axib_b.start();
        out_io_c2h_cmd.start();
        out_io_c2h_data.start();
        out_io_h2c_cmd.start();
		io_num_rpcs	<= 32;
        #50;
		io_start	<= 1;
		// data,last,strb
		// in_io_axib_w.write({512'h0,1'h0,64'h0});
end

always #5 clock=~clock;
endmodule