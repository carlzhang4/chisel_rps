`timescale 1ns / 1ns
module tb_ProducerConsumer(

    );

reg                 clock                         =0;
reg                 reset                         =0;
wire                io_in_ready                   ;
reg                 io_in_valid                   =0;
reg       [7:0]     io_in_bits                    =0;
reg                 io_out_0_ready                =0;
wire                io_out_0_valid                ;
wire      [7:0]     io_out_0_bits                 ;
reg                 io_out_1_ready                =0;
wire                io_out_1_valid                ;
wire      [7:0]     io_out_1_bits                 ;
reg                 io_out_2_ready                =0;
wire                io_out_2_valid                ;
wire      [7:0]     io_out_2_bits                 ;
reg                 io_out_3_ready                =0;
wire                io_out_3_valid                ;
wire      [7:0]     io_out_3_bits                 ;

IN#(8)in_io_in(
        clock,
        reset,
        {io_in_bits},
        io_in_valid,
        io_in_ready
);
// 
// 8'h0

OUT#(8)out_io_out_0(
        clock,
        reset,
        {io_out_0_bits},
        io_out_0_valid,
        io_out_0_ready
);
// 
// 8'h0

OUT#(8)out_io_out_1(
        clock,
        reset,
        {io_out_1_bits},
        io_out_1_valid,
        io_out_1_ready
);
// 
// 8'h0

OUT#(8)out_io_out_2(
        clock,
        reset,
        {io_out_2_bits},
        io_out_2_valid,
        io_out_2_ready
);
// 
// 8'h0

OUT#(8)out_io_out_3(
        clock,
        reset,
        {io_out_3_bits},
        io_out_3_valid,
        io_out_3_ready
);
// 
// 8'h0


ProducerConsumer ProducerConsumer_inst(
        .*
);

/*

in_io_in.write({8'h0});

*/

initial begin
        reset <= 1;
        clock = 1;
        #1000;
        reset <= 0;
        #100;
        out_io_out_0.start();
        out_io_out_1.start();
        out_io_out_2.start();
        out_io_out_3.start();
        #50;
		in_io_in.write({8'h0});
		in_io_in.write({8'h1});
		in_io_in.write({8'h2});
		in_io_in.write({8'h3});
		in_io_in.write({8'h4});
		in_io_in.write({8'h5});
		in_io_in.write({8'h6});
		in_io_in.write({8'h7});
		in_io_in.write({8'h8});
		in_io_in.write({8'h9});
		in_io_in.write({8'ha});
		in_io_in.write({8'hb});
		in_io_in.write({8'hc});
		in_io_in.write({8'hd});
		in_io_in.write({8'he});
		in_io_in.write({8'hf});
		#10;
		io_out_1_ready	<= 0;
		#1000;
		io_out_1_ready	<= 1;

end

always #5 clock=~clock;
endmodule