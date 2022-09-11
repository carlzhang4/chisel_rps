module tb_CreditQ(

    );

reg                 clock                         =0;
reg                 reset                         =0;
wire                io_in_ready                   ;
reg                 io_in_valid                   =0;
reg                 io_in_bits                    =0;
reg                 io_out_ready                  =0;
wire                io_out_valid                  ;
wire                io_out_bits                   ;

IN#(1)in_io_in(
        clock,
        reset,
        {io_in_bits},
        io_in_valid,
        io_in_ready
);
// 
// 1'h0

// OUT#(1)out_io_out(
//         clock,
//         reset,
//         {io_out_bits},
//         io_out_valid,
//         io_out_ready
// );
// 
// 1'h0


CreditQ CreditQ_inst(
        .*
);

/*

in_io_in.write({1'h0});

*/

initial begin
        reset <= 1;
        clock = 1;
        #1000;
        reset <= 0;
		io_out_ready <= 0;
        #50;
		in_io_in.write(1'h0);
		in_io_in.write(1'h0);
		in_io_in.write(1'h0);
		in_io_in.write(1'h0);
		in_io_in.write(1'h0);
		in_io_in.write(1'h0);
		in_io_in.write(1'h0);
		in_io_in.write(1'h0);
		in_io_in.write(1'h0);
		in_io_in.write(1'h0);
		in_io_in.write(1'h0);
		#110;
        // out_io_out.start();
		io_out_ready <= 1;
end

always #5 clock=~clock;
endmodule