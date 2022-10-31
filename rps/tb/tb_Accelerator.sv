`timescale 1ns / 1ns
module tb_Accelerator(

    );

reg                 clock                         =0;
reg                 reset                         =0;
wire                io_in_ready                   ;
reg                 io_in_valid                   =0;
reg                 io_in_bits_last               =0;
reg       [511:0]   io_in_bits_data               =0;
reg                 io_out_ready                  =0;
wire                io_out_valid                  ;
wire                io_out_bits_last              ;
wire      [511:0]   io_out_bits_data              ;

IN#(513)in_io_in(
        clock,
        reset,
        {io_in_bits_last,io_in_bits_data},
        io_in_valid,
        io_in_ready
);
// last, data
// 1'h0, 512'h0

OUT#(513)out_io_out(
        clock,
        reset,
        {io_out_bits_last,io_out_bits_data},
        io_out_valid,
        io_out_ready
);
// last, data
// 1'h0, 512'h0


Accelerator Accelerator_inst(
        .*
);

/*
last,data
in_io_in.write({1'h0,512'h0});

*/

initial begin
        reset <= 1;
        clock = 1;
        #1000;
        reset <= 0;
        #100;
        out_io_out.start();
        #50;
		in_io_in.write({1'h0,512'h0});
		in_io_in.write({1'h0,512'h0});
		in_io_in.write({1'h0,512'h0});
		in_io_in.write({1'h1,512'h0});

		in_io_in.write({1'h0,512'h1});
		in_io_in.write({1'h0,512'h1});
		in_io_in.write({1'h0,512'h1});
		in_io_in.write({1'h1,512'h1});

		in_io_in.write({1'h0,512'h2});
		in_io_in.write({1'h0,512'h2});
		in_io_in.write({1'h0,512'h2});
		in_io_in.write({1'h1,512'h2});

		in_io_in.write({1'h0,512'h3});
		in_io_in.write({1'h0,512'h3});
		in_io_in.write({1'h0,512'h3});
		in_io_in.write({1'h1,512'h3});

		in_io_in.write({1'h0,512'h4});
		in_io_in.write({1'h0,512'h4});
		in_io_in.write({1'h0,512'h4});
		in_io_in.write({1'h1,512'h4});

		in_io_in.write({1'h0,512'h5});
		in_io_in.write({1'h0,512'h5});
		in_io_in.write({1'h0,512'h5});
		in_io_in.write({1'h1,512'h5});

		in_io_in.write({1'h0,512'h6});
		in_io_in.write({1'h0,512'h6});
		in_io_in.write({1'h0,512'h6});
		in_io_in.write({1'h1,512'h6});

		in_io_in.write({1'h0,512'h7});
		in_io_in.write({1'h0,512'h7});
		in_io_in.write({1'h0,512'h7});
		in_io_in.write({1'h1,512'h7});

		in_io_in.write({1'h0,512'h8});
		in_io_in.write({1'h0,512'h8});
		in_io_in.write({1'h0,512'h8});
		in_io_in.write({1'h1,512'h8});

		in_io_in.write({1'h0,512'h9});
		in_io_in.write({1'h0,512'h9});
		in_io_in.write({1'h0,512'h9});
		in_io_in.write({1'h1,512'h9});


		in_io_in.write({1'h0,512'h10});
		in_io_in.write({1'h0,512'h10});
		in_io_in.write({1'h0,512'h10});
		in_io_in.write({1'h1,512'h10});

		in_io_in.write({1'h0,512'h11});
		in_io_in.write({1'h0,512'h11});
		in_io_in.write({1'h0,512'h11});
		in_io_in.write({1'h1,512'h11});

		in_io_in.write({1'h0,512'h12});
		in_io_in.write({1'h0,512'h12});
		in_io_in.write({1'h0,512'h12});
		in_io_in.write({1'h1,512'h12});

		in_io_in.write({1'h0,512'h13});
		in_io_in.write({1'h0,512'h13});
		in_io_in.write({1'h0,512'h13});
		in_io_in.write({1'h1,512'h13});

		in_io_in.write({1'h0,512'h14});
		in_io_in.write({1'h0,512'h14});
		in_io_in.write({1'h0,512'h14});
		in_io_in.write({1'h1,512'h14});

		in_io_in.write({1'h0,512'h15});
		in_io_in.write({1'h0,512'h15});
		in_io_in.write({1'h0,512'h15});
		in_io_in.write({1'h1,512'h15});

		in_io_in.write({1'h0,512'h16});
		in_io_in.write({1'h0,512'h16});
		in_io_in.write({1'h0,512'h16});
		in_io_in.write({1'h1,512'h16});
end

always #5 clock=~clock;
endmodule