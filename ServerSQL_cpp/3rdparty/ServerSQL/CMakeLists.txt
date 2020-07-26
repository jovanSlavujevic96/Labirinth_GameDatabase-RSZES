cmake_minimum_required (VERSION 2.8.11)
project (ServerSQL)

add_library (ServerSQL SHARED src/Server.cpp src/SQL.cpp)
target_include_directories(ServerSQL PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include)

set(CMAKE_CXX_FLAGS "-fPIC -std=c++17 -O3 -ggdb -pthread -Wall -Wextra")
