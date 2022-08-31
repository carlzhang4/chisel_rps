#include <QDMAController.h>
#include <unistd.h>
#include <gflags/gflags.h>
#include "src/debug.hpp"

DEFINE_string(type, "none", "node type");

int main(int argc, char** argv){
	gflags::ParseCommandLineFlags(&argc, &argv, true);
	unsigned char pci_bus = 0x1a;
	init(pci_bus);
	volatile uint32_t * bar = (volatile uint32_t*)getLiteAddr(pci_bus);
	
	if(FLAGS_type.compare("client") == 0){
		cout<<"Client Check Status:"<<endl;
		print_reporters_client(bar);
	}else if(FLAGS_type.compare("bs") == 0){
		cout<<"BS Check Status"<<endl;	
		print_reporters_bs(bar);
	}else{
		cout<<"Invalid type"<<endl;
	}    
    return 0;
}