 cmake_minimum_required(VERSION 3.10)
 project(Bento4Android)

 set(CMAKE_SYSTEM_NAME Android)
 set(CMAKE_ANDROID_NDK $ENV{ANDROID_NDK})
 set(CMAKE_SYSTEM_VERSION 21) # API 21 minimum
 set(CMAKE_ANDROID_ARCH_ABI arm64-v8a)
 set(CMAKE_ANDROID_STL_TYPE c++_shared)

 # Fichiers sources C++
 set(SOURCE_FILES Bento4Wrapper.cpp)

 add_library(bento4 SHARED ${SOURCE_FILES})

 # Inclure les headers
 include_directories(${CMAKE_SOURCE_DIR}/include)

 # Lier avec log (Android)
 target_link_libraries(bento4 log)
