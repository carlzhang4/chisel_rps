#include <QDMAController.h>
#include <unistd.h>
#include "src/debug.hpp"

int main(){
	unsigned char pci_bus = 0x3d;
	init(pci_bus);
	volatile uint32_t * bar = (volatile uint32_t*)getLiteAddr(pci_bus);
	print_reporters(bar);
}