set_property PACKAGE_PIN D32 [get_ports led]
set_property IOSTANDARD LVCMOS18 [get_ports led]

create_clock -period 10.000 -name sysclk2 -add [get_ports sysClkP]

set_property PACKAGE_PIN BJ43 [get_ports sysClkP]
set_property IOSTANDARD DIFF_SSTL12 [get_ports sysClkP]

set_false_path -from [get_cells userRstn_reg]
set_false_path -from [get_cells netRstn_reg]
set_false_path -to [get_cells netRstn_reg]

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

#cmac
set_false_path -from [get_clocks -of_objects [get_pins -hier -filter {NAME =~ */gtye4_channel_gen.gen_gtye4_channel_inst[*].GTYE4_CHANNEL_PRIM_INST/RXOUTCLK}]] -to [get_clocks -of_objects [get_pins -hier -filter {NAME =~ */gtye4_channel_gen.gen_gtye4_channel_inst[*].GTYE4_CHANNEL_PRIM_INST/TXOUTCLK}]]

create_pblock pblock_qdma
add_cells_to_pblock [get_pblocks pblock_qdma] [get_cells -quiet [list qdma]]
resize_pblock [get_pblocks pblock_qdma] -add {SLR0}

create_pblock pblock_roce
resize_pblock [get_pblocks pblock_roce] -add {SLR0:SLR1}
add_cells_to_pblock pblock_roce [get_cells [list roce/roce]]
add_cells_to_pblock pblock_roce [get_cells [list roce1/roce]]
# add_cells_to_pblock pblock_roce [get_cells [list clientAndCS]] #when there is ila in it, comment this line

create_pblock pblock_1
resize_pblock [get_pblocks pblock_1] -add {SLR1}
add_cells_to_pblock pblock_1 [get_cells [list roce/ip]]
add_cells_to_pblock pblock_1 [get_cells [list roce1/ip]]
add_cells_to_pblock pblock_1 [get_cells [list clientAndCS]]
add_cells_to_pblock pblock_1 [get_cells [list clientAndCS1]]

create_pblock pblock_cmac
add_cells_to_pblock [get_pblocks pblock_cmac] [get_cells -quiet [list roce/cmac]]
add_cells_to_pblock [get_pblocks pblock_cmac] [get_cells -quiet [list roce1/cmac]]
resize_pblock [get_pblocks pblock_cmac] -add {SLR2}
