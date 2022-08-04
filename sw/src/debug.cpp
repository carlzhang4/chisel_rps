#include "debug.hpp"

void print_reporters(volatile uint32_t *bar){
	int offset_RPSCounter = 512 + 100;
    printf("QDMAControl::aw_fire                                        :%d\n", bar[0+offset_RPSCounter]);
	printf("QDMAControl::w_fire                                         :%d\n", bar[1+offset_RPSCounter]);
	printf("Client_SendMetaFire                                         :%d\n", bar[2+offset_RPSCounter]);
	printf("Client_SendDataFire                                         :%d\n", bar[3+offset_RPSCounter]);
	printf("Client_SendDataLast                                         :%d\n", bar[4+offset_RPSCounter]);
	printf("Client_RecvMetaFire                                         :%d\n", bar[5+offset_RPSCounter]);
	printf("Client_RecvDataFire                                         :%d\n", bar[6+offset_RPSCounter]);
	printf("Client_RecvDataLast                                         :%d\n", bar[7+offset_RPSCounter]);
	printf("CS_SendMetaFire                                             :%d\n", bar[8+offset_RPSCounter]);
	printf("CS_SendDataFire                                             :%d\n", bar[9+offset_RPSCounter]);
	printf("CS_SendDataLast                                             :%d\n", bar[10+offset_RPSCounter]);
	printf("CS_RecvMetaFire                                             :%d\n", bar[11+offset_RPSCounter]);
	printf("CS_RecvDataFire                                             :%d\n", bar[12+offset_RPSCounter]);
	printf("CS_RecvDataLast                                             :%d\n", bar[13+offset_RPSCounter]);
	printf("ClientReqHandler_RecvMetaFire                               :%d\n", bar[14+offset_RPSCounter]);
	printf("ClientReqHandler_RecvDataFire                               :%d\n", bar[15+offset_RPSCounter]);
	printf("ClientReqHandler_RecvDataLast                               :%d\n", bar[16+offset_RPSCounter]);
	printf("ClientReqHandler_Meta2HostFire                              :%d\n", bar[17+offset_RPSCounter]);
	printf("ClientReqHandler_Data2HostFire                              :%d\n", bar[18+offset_RPSCounter]);
	printf("ClientReqHandler_Data2HostLast                              :%d\n", bar[19+offset_RPSCounter]);
	printf("ClientReqHandler_MetaFromHostFire                           :%d\n", bar[20+offset_RPSCounter]);
	printf("ClientReqHandler_SendMetaFire                               :%d\n", bar[21+offset_RPSCounter]);
	printf("ClientReqHandler_SendDataFire                               :%d\n", bar[22+offset_RPSCounter]);
	printf("ClientReqHandler_SendDataLast                               :%d\n", bar[23+offset_RPSCounter]);
	printf("CSReqHandler_RecvMetaFire                                   :%d\n", bar[24+offset_RPSCounter]);
	printf("CSReqHandler_RecvDataFire                                   :%d\n", bar[25+offset_RPSCounter]);
	printf("CSReqHandler_RecvDataLast                                   :%d\n", bar[26+offset_RPSCounter]);
	printf("CSReqHandler_Meta2HostFire                                  :%d\n", bar[27+offset_RPSCounter]);
	printf("CSReqHandler_Data2HostFire                                  :%d\n", bar[28+offset_RPSCounter]);
	printf("CSReqHandler_Data2HostLast                                  :%d\n", bar[29+offset_RPSCounter]);
	printf("CSReqHandler_MetaFromHostFire                               :%d\n", bar[30+offset_RPSCounter]);
	printf("CSReqHandler_SendMetaFire                                   :%d\n", bar[31+offset_RPSCounter]);
	printf("CSReqHandler_SendDataFire                                   :%d\n", bar[32+offset_RPSCounter]);
	printf("CSReqHandler_SendDataLast                                   :%d\n", bar[33+offset_RPSCounter]);
	printf("\n");

	int offset_RPSReporter = 512 + 100 + 64;
    printf("QDMAControl::reg_max                                        :%d\n", bar[0+offset_RPSReporter]);
	printf("Client::reg_time_high                                       :%d\n", bar[1+offset_RPSReporter]);
	printf("Client::reg_time_low                                        :%d\n", bar[2+offset_RPSReporter]);
	printf("Client::reg_latency_high                                    :%d\n", bar[3+offset_RPSReporter]);
	printf("Client::reg_latency_high                                    :%d\n", bar[4+offset_RPSReporter]);
}