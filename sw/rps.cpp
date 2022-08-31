#include <QDMAController.h>
#include <immintrin.h>
#include <unistd.h>
#include <vector>
#include <thread>
#include <mutex>
#include <gflags/gflags.h>
#include "src/util.hpp"
#include "src/debug.hpp"

DEFINE_string(type, "none", "node type");

using namespace std;
std::mutex IO_LOCK;

uint32_t num_rpcs = 1024*1024*16;//16*1024*1024;//1024*1024;

#define HOST_MEM_PARTITION (1L*1024*1024*1024)
#define CS_HOST_MEM_OFFSET (1L*1024*1024*1024)
#define PRINT_MOD (100000)
#define DEBUG_PRINT 1
void sub_task_client_req(int thread_index, __m512i* bridge, uint32_t num_rpcs, size_t* client_buffer){
	while(thread_index != sched_getcpu()){
		std::this_thread::sleep_for(std::chrono::milliseconds(20));//wait set affinity success
	}
	{
		std::lock_guard<std::mutex> guard(IO_LOCK);
		LOG_I("Thread [%2d] has been moved to core [%2d]",thread_index,sched_getcpu());
	}

	__m512i client_req_response;
	for(int i=0;i<8;i++){
		client_req_response[i] = 0;
	}
    client_req_response[7]=1l<<32;

	size_t cur_word_client = 0;
	size_t MAX_INDEX = HOST_MEM_PARTITION/(sizeof(size_t));
	size_t word2size_t = 64/sizeof(size_t);
	while(true){
		size_t index_client = (cur_word_client*word2size_t)%MAX_INDEX;
		if(client_buffer[index_client] == cur_word_client*64+1){
            if(DEBUG_PRINT && cur_word_client%PRINT_MOD==0){
                cout<<"cur_word_client:"<<cur_word_client<<endl;
            }
            cur_word_client++;
            _mm512_stream_si512 (bridge, client_req_response);
        }

		if(cur_word_client==num_rpcs){
			break;
		}
	}
}

void sub_task_cs_req(int thread_index, __m512i* bridge, uint32_t num_rpcs, size_t* cs_buffer){
	while(thread_index != sched_getcpu()){
		std::this_thread::sleep_for(std::chrono::milliseconds(20));//wait set affinity success
	}
	{
		std::lock_guard<std::mutex> guard(IO_LOCK);
		LOG_I("Thread [%2d] has been moved to core [%2d]",thread_index,sched_getcpu());
	}

	__m512i cs_req_response;
	for(int i=0;i<8;i++){
		cs_req_response[i] = 0;
	}
    cs_req_response[7]=2l<<32;

	size_t cur_word_cs = 0;
	size_t MAX_INDEX = HOST_MEM_PARTITION/(sizeof(size_t));
	size_t word2size_t = 64/sizeof(size_t);
	while(true){
		size_t index_cs = (cur_word_cs*word2size_t)%MAX_INDEX;
		if(cs_buffer[index_cs] == cur_word_cs*64+CS_HOST_MEM_OFFSET+1){
            if(DEBUG_PRINT && cur_word_cs%PRINT_MOD==0){
                cout<<"cur_word_cs:"<<cur_word_cs<<endl;
            }
            cur_word_cs++;
            _mm512_stream_si512 (bridge+16, cs_req_response);
        }

		if(cur_word_cs==num_rpcs){
			break;
		}
	}
}

void run_client(){
	unsigned char pci_bus = 0x1a;
	init(pci_bus);
	volatile uint32_t * bar = (volatile uint32_t*)getLiteAddr(pci_bus);

	bar[103] = 0;
	bar[100] = 1;
	sleep(1);
	bar[100] = 0;//reset
	sleep(1);

	//qp_init start
	bar[101] = 0;
	bar[101] = 1;
	sleep(1);
	bar[101] = 0;
	bar[101] = 1;

	sleep(1);
	bar[101] = 0;
	bar[102] = num_rpcs;
	
	bar[103] = 1;//start
	sleep(1);
	bar[103] = 0;
	print_reporters_client(bar);
}

void run_bs(){
	unsigned char pci_bus = 0x1a;
    size_t size = 2L*1024*1024*1024;

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

	// print_reporters(bar);
	// exit(1);

	bar[107] = 0;//start qp_init
	bar[106] = 1;//reset
	sleep(1);
	bar[106] = 0;
	sleep(1);

    bar[100] = (uint32_t)((unsigned long)p>>32);
	bar[101] = (uint32_t)((unsigned long)p);

	bar[112] = (uint32_t)0x01bda8c0; //remote ip: 192.168.189.1
	bar[113] = (uint32_t)0x02bda8c0; //local ip: 192.168.189.2 (02/189/168/192)

	bar[108] = (uint32_t)1;//local qpn
	bar[109] = (uint32_t)0x2001;//local psn
	bar[110] = (uint32_t)0x1001;//remote psn
	bar[111] = (uint32_t)1;//remote qpn
	bar[107] = 1;

	sleep(1);

	bar[107] = 0;
	bar[108] = (uint32_t)2;//local qpn
	bar[109] = (uint32_t)0x2002;//local psn
	bar[110] = (uint32_t)0x1002;//remote psn
	bar[111] = (uint32_t)2;//remote qpn
	bar[107] = 1;
	

	vector<thread> threads(2);
	//client req handler
	threads[0] = thread(sub_task_client_req, 1, bridge, num_rpcs, client_buffer);
	set_cpu(threads[0], 1);

	//cs req handler
	threads[1] = thread(sub_task_cs_req, 2, bridge, num_rpcs, cs_buffer);
	set_cpu(threads[1], 2);

	for(int i=0;i<2;i++){
		threads[i].join();
	}

    print_reporters_bs(bar);
    printCounters(pci_bus);

}
int main(int argc, char** argv){
	gflags::ParseCommandLineFlags(&argc, &argv, true);
	if(FLAGS_type.compare("client") == 0){
		cout<<"Client start:"<<endl;
		run_client();
	}else if(FLAGS_type.compare("bs") == 0){
		cout<<"BS start"<<endl;	
		run_bs();
	}else{
		cout<<"Invalid type"<<endl;
	}    
    return 0;
}