cmake_minimum_required(VERSION 3.7)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED True)

project(rps LANGUAGES CXX VERSION 0.1)

add_definitions(-mavx512f)
add_compile_options(-O3 -g -march=native)

aux_source_directory(src SRCS)

add_executable(rps
	${SRCS}
	rps.cpp
)

target_link_libraries(rps
	qdma
	pthread
	numa
)

add_executable(check_status
	${SRCS}
	check_status.cpp
)

target_link_libraries(check_status
	qdma
	pthread
)