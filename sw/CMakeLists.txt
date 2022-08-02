cmake_minimum_required(VERSION 3.7)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED True)

project(rps LANGUAGES CXX VERSION 0.1)

add_definitions(-mavx512f)
add_compile_options(-O3 -march=native)


add_executable(rps
	rps.cpp
)
#target_include_directories(rps PUBLIC
#	${CMAKE_SOURCE_DIR}/dma
#	${CMAKE_CURRENT_SOURCE_DIR}/mmio
#)
target_link_libraries(rps
	qdma
	pthread
)