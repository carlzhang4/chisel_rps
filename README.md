# Rc4ml Chisel Template
```
Please make sure you have already installed mill
$ git clone https://github.com/RC4ML/chisel_template.git
$ cd chisel_template
$ git submodule add git@github.com:carlzhang4/common.git common
$ git submodule add git@github.com:carlzhang4/qdma.git qdma

Generate your first module:
$ mill project_foo Test
Corresponding sv file can be found under Verilog folder

Generate a QDMATop module:
$ mill project_foo QDMATop
Corresponding sv file can be found under Verilog folder
```

# How to generate a QDMA benchmark project
```
First, create a vivado project, vivado version must be 2020.2 or later

$ mill project_foo QDMATop
This will generate a QDMATop.sv under Verilog, copy and add it to your vivado project.

Copy xdc file (./qdma/src/sv) to your vivado project and add it as constraint.

Generate a qdma IP in vivado catalog, it's name must be QDMABlackBox.

Then you can generate your bitstream.
```

# How to simplify your workflow with several script


### postElaborating.py
```
Create a config.json file in the project root dir, fill it with following contents
	{
		"project_foo":{
			"destIPRepoPath" : "/path to your vivado project/example.srcs/sources_1/ip",
			"destSrcPath" : "/path to your vivado project/example.srcs/sources_1/new"
		}
	}
destSrcPath is where you want to put your sv file
destIPRepoPath is where you want to put your vivado ip if you have use chisel based ila in your project.

$ python3 postElaborating.py project_foo QDMATop -t -p
Above command will help you move sv file and generate some tcl if you have used it in your project.
```

### instant.py
```
After sv file has been generated, run following command to generate a testbench, replace Test with your module name.
$ python3 instant.py Test
```