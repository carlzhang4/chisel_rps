`timescale 1ns / 1ns
module tb_CSReqHandler(

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


CSReqHandler CSReqHandler_inst(
        .*
);

/*
qpn,msg_num,pkg_num,pkg_total
in_io_recv_meta.write({16'h0,24'h0,21'h0,21'h0});

last,data,keep
in_io_recv_data.write({1'h0,512'h0,64'h0});

data
in_io_meta_from_host.write({512'h0});

*/

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
		in_io_recv_meta.write({16'h2,24'h0,21'h0,21'h0});
		in_io_recv_meta.write({16'h2,24'h0,21'h0,21'h0});
		in_io_recv_meta.write({16'h2,24'h0,21'h0,21'h0});
		in_io_recv_meta.write({16'h2,24'h0,21'h0,21'h0});

		#1000;
		in_io_meta_from_host.write({512'h0});
		in_io_meta_from_host.write({512'h0});
		in_io_meta_from_host.write({512'h0});
		in_io_meta_from_host.write({512'h0});
end

always #5 clock=~clock;
endmodule