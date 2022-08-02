#include <QDMAController.h>
#include <immintrin.h>
#include <unistd.h>
using namespace std;

void print_reporters(volatile uint32_t *bar){
    printf("Client_SendMetaFire                 :%d\n",bar[512+100+0]);
    printf("Client_SendDataFire                 :%d\n",bar[512+100+1]);
    printf("Client_SendDataLast                 :%d\n",bar[512+100+2]);
    printf("Client_RecvMetaFire                 :%d\n",bar[512+100+3]);
    printf("Client_RecvDataFire                 :%d\n",bar[512+100+4]);
    printf("Client_RecvDataLast                 :%d\n",bar[512+100+5]);
    printf("CS_SendMetaFire                     :%d\n",bar[512+100+6]);
    printf("CS_SendDataFire                     :%d\n",bar[512+100+7]);
    printf("CS_SendDataLast                     :%d\n",bar[512+100+8]);
    printf("CS_RecvMetaFire                     :%d\n",bar[512+100+9]);
    printf("CS_RecvDataFire                     :%d\n",bar[512+100+10]);
    printf("CS_RecvDataLast                     :%d\n",bar[512+100+11]);
    printf("ClientReqHandler_RecvMetaFire       :%d\n",bar[512+100+12]);
    printf("ClientReqHandler_RecvDataFire       :%d\n",bar[512+100+13]);
    printf("ClientReqHandler_RecvDataLast       :%d\n",bar[512+100+14]);
    printf("ClientReqHandler_Meta2HostFire      :%d\n",bar[512+100+15]);
    printf("ClientReqHandler_Data2HostFire      :%d\n",bar[512+100+16]);
    printf("ClientReqHandler_Data2HostLast      :%d\n",bar[512+100+17]);
    printf("ClientReqHandler_MetaFromHostFire   :%d\n",bar[512+100+18]);
    printf("ClientReqHandler_SendMetaFire       :%d\n",bar[512+100+19]);
    printf("ClientReqHandler_SendDataFire       :%d\n",bar[512+100+20]);
    printf("ClientReqHandler_SendDataLast       :%d\n",bar[512+100+21]);
    printf("CSReqHandler_RecvMetaFire           :%d\n",bar[512+100+22]);
    printf("CSReqHandler_RecvDataFire           :%d\n",bar[512+100+23]);
    printf("CSReqHandler_RecvDataLast           :%d\n",bar[512+100+24]);
    printf("CSReqHandler_Meta2HostFire          :%d\n",bar[512+100+25]);
    printf("CSReqHandler_Data2HostFire          :%d\n",bar[512+100+26]);
    printf("CSReqHandler_Data2HostLast          :%d\n",bar[512+100+27]);
    printf("CSReqHandler_MetaFromHostFire       :%d\n",bar[512+100+28]);
    printf("CSReqHandler_SendMetaFire           :%d\n",bar[512+100+29]);
    printf("CSReqHandler_SendDataFire           :%d\n",bar[512+100+30]);
    printf("CSReqHandler_SendDataLast           :%d\n",bar[512+100+31]);
}
int main(){
    unsigned char pci_bus = 0x3d;
    size_t size = 2L*1024*1024*1024;
    size_t HOST_MEM_PARTITION = 1L*1024*1024*1024;
    uint32_t num_rpsc = 16*1024*1024;

    init(pci_bus);
    void * dmaBuffer = qdma_alloc(size,pci_bus);
    size_t *p = (size_t *) dmaBuffer;
    size_t *client_buffer = p;
    size_t *cs_buffer = p+(HOST_MEM_PARTITION/sizeof(size_t));
    for(size_t i=0;i<size/sizeof(size_t);i++){
		p[i]=0;
	}
    volatile uint32_t * bar = (volatile uint32_t*)getLiteAddr(pci_bus);
    __m512i* bridge = (__m512i*)getBridgeAddr(pci_bus);

    for(int i=0;i<1;i++){
		writeConfig(0x1408/4,i,pci_bus);
		uint32_t tag = readConfig(0x140c/4,pci_bus);
		bar[103] = tag;
		bar[104] = i+1;
		printf("tag:%d\n",tag&0x7f);
	}

    __m512i client_req_response;
    __m512i cs_req_response;
	for(int i=0;i<8;i++){
		client_req_response[i] = 0;
        cs_req_response[i] = 0;
	}
    client_req_response[7]=1l<<32;
    cs_req_response[7]=2l<<32;

    bar[105] = 0;//start
	bar[106] = 1;//reset
	bar[106] = 0;

    bar[100] = (uint32_t)((unsigned long)p>>32);
	bar[101] = (uint32_t)((unsigned long)p);
    bar[102] = num_rpsc;

	sleep(1);
    bar[105] = 1;//start

	size_t CS_HOST_MEM_OFFSET = 1024L*1024*1024;
    size_t cur_word_client = 0;
    size_t cur_word_cs = 0;
    size_t MAX_INDEX=HOST_MEM_PARTITION/(sizeof(size_t));
    size_t word2size_t = 64/sizeof(size_t);
    while(true){
        size_t index_client = (cur_word_client*word2size_t)%MAX_INDEX;
        size_t index_cs = (cur_word_cs*word2size_t)%MAX_INDEX;

        if(client_buffer[index_client] == cur_word_client*64+1){
            if(cur_word_client%10000==0){
                cout<<"cur_word_client:"<<cur_word_client<<endl;
            }
            cur_word_client++;
            _mm512_stream_si512 (bridge, client_req_response);
        }

        if(cs_buffer[index_cs] == cur_word_cs*64+CS_HOST_MEM_OFFSET+1){
            if(cur_word_cs%10000==0){
                cout<<"cur_word_cs:"<<cur_word_cs<<endl;
            }
            cur_word_cs++;
            _mm512_stream_si512 (bridge, cs_req_response);
        }

		if(cur_word_client==num_rpsc && cur_word_cs==num_rpsc){
			break;
		}
    }

    print_reporters(bar);
    // printCounters(pci_bus);
    return 0;
}