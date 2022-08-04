#ifndef __DEBUG_HPP__
#define __DEBUG_HPP__

#include <stdio.h>
#include <string>
#include <unistd.h>
#include <sys/mman.h>

using namespace std;

void print_reporters(volatile uint32_t *bar);

#endif