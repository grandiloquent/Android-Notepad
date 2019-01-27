cmake_minimum_required(VERSION 3.4.1)

set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution)

#add_library(libiconv SHARED IMPORTED)
#set_target_properties(libiconv PROPERTIES IMPORTED_LOCATION
#        ${distribution_DIR}/libiconv/lib/${ANDROID_ABI}/libiconv.so)

add_library(libsqlite SHARED
        ${distribution_DIR}/libsqlite/sqlite3.c)

add_library(libmongoose SHARED
        ${distribution_DIR}/libmongoose/mongoose.c)

add_library(main SHARED
        main.c
        strbuf.c)

target_include_directories(main PRIVATE
        ${distribution_DIR}/libsqlite
        ${distribution_DIR}/libmongoose)

target_link_libraries(main
        libsqlite
        libmongoose
        android
        log)