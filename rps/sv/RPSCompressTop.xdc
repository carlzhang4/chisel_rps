set_property PACKAGE_PIN D32 [get_ports led]
set_property IOSTANDARD LVCMOS18 [get_ports led]

create_clock -period 10.000 -name sysclk2 -add [get_ports sysClkP]

set_property PACKAGE_PIN BJ43 [get_ports sysClkP]
set_property IOSTANDARD DIFF_SSTL12 [get_ports sysClkP]

set_false_path -from [get_cells userRstn_reg]

#qdma
create_clock -period 10.000 -name sys_clk [get_ports qdma_pin_sys_clk_p]

set_input_delay 5 -clock [get_clocks sys_clk] [get_ports qdma_pin_sys_rst_n]
set_false_path -from [get_ports qdma_pin_sys_rst_n]
set_property PULLUP true [get_ports qdma_pin_sys_rst_n]
set_property IOSTANDARD LVCMOS18 [get_ports qdma_pin_sys_rst_n]
set_property PACKAGE_PIN BH26 [get_ports qdma_pin_sys_rst_n]
set_property CONFIG_VOLTAGE 1.8 [current_design]

set_property PACKAGE_PIN AR14 [get_ports qdma_pin_sys_clk_n]
set_property PACKAGE_PIN AR15 [get_ports qdma_pin_sys_clk_p]
set_false_path -from [get_cells -regexp {qdma/axil2reg/reg_control_[0-9]*_reg\[.*]}]
set_false_path -to [get_cells -regexp {qdma/axil2reg/reg_status_[0-9]*_reg\[.*]}]
set_false_path -to [get_pins -hier {*sync_reg[0]/D}]

create_pblock pblock_qdma
add_cells_to_pblock [get_pblocks pblock_qdma] [get_cells -quiet [list qdma]]
resize_pblock [get_pblocks pblock_qdma] -add {SLR0}
