set_property C_USER_SCAN_CHAIN 1 [get_debug_cores dbg_hub]
connect_debug_port dbg_hub/clk [get_nets -hier -filter {NAME =~ */mmcmGlbl/mmcmGlbl_io_CLKOUT0}]

set_false_path -to [get_pins -hier *sync_reg[0]/D]

set_property PACKAGE_PIN D32 [get_ports led]
set_property IOSTANDARD LVCMOS18 [get_ports led]

create_clock -period 10.000 -name sysclk2 -add [get_ports sysClkP]

set_property PACKAGE_PIN BJ43 [get_ports sysClkP]
set_property IOSTANDARD DIFF_SSTL12 [get_ports sysClkP]

set_false_path -from [get_clocks -of_objects [get_pins -hier -filter {NAME =~ */mmcmAxi/mmcm4_adv/CLKOUT0}]] -to [get_clocks -of_objects [get_pins -hier -filter {NAME =~ */mmcmGlbl/mmcm4_adv/CLKOUT0}] -filter {IS_GENERATED}]

set_false_path -from [get_cells hbmRstn_reg]
#set_false_path -to [get_cells hbmRstn_reg]

#set_false_path -to [get_pins hbmDriver/io_hbm_rstn_r_*_reg_srl4/D] 

set_false_path -from [get_clocks -of_objects [get_pins hbmDriver/mmcmGlbl/mmcm4_adv/CLKOUT0]] -to [get_clocks -of_objects [get_pins hbmDriver/mmcmAxi/mmcm4_adv/CLKOUT0]]
set_false_path -from [get_clocks -of_objects [get_pins hbmDriver/mmcmGlbl/mmcm4_adv/CLKOUT3]] -to [get_clocks -of_objects [get_pins hbmDriver/mmcmAxi/mmcm4_adv/CLKOUT0]]