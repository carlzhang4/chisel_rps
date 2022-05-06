# RC4ML Chisel Template
```
Please make sure you have already installed mill https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html

$ git clone https://github.com/RC4ML/chisel_template.git
$ cd chisel_template
$ git submodule add git@github.com:carlzhang4/common.git common
$ git submodule add git@github.com:carlzhang4/qdma.git qdma

Generate your first module:
$ mill project_foo Foo
Corresponding sv file Foo.sv can be found under Verilog folder

When you write a new module and want to generate its verilog, edit Elaborate.scala
For example a new Bar module, put following code near other cases in Elaborate.scala.
"case "Foo" => stage.execute(arr,Seq(ChiselGeneratorAnnotation(() => new Foo()),dir)) "
```

# How to generate a QDMA benchmark project
```
First, create a vivado project, vivado version must either of 2020.01/2020.02/2021.01

Modify two lines in ./qdma/src/QDMATop.scala, replace with your vivado version and your vivado project's IP location

$ mill project_foo QDMATop
This will generate a QDMATop.sv under Verilog folder, copy and add it to your vivado project. 

And it will also print several tcl commands, starts with create_ip xxxx, ends with update_compile_order -fileset sources_1.

Copy these tcls and execute them in your vivado project's tcl console, this will help you to generate a QDMA IP.

Copy xdc file (./qdma/sv) to your vivado project and add it as constraint.

Then you can generate your bitstream.

Corresponding benchmark software and corresponding driver could be found in https://github.com/carlzhang4/qdma_improve

You can also write your own top file by refering ./qdma/src/QDMATop.scala.
```

# How to simplify your workflow with several scripts


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
Move ./common/sv/TESTER.sv to your vivado project, and add it as simulation source

If your sv module file has been generated, run following command to generate a testbench, replace Foo with your module name.
$ python3 instant.py Foo
```