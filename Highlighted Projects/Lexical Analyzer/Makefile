CPP = cl /std:c11 /MTD /Z7 /DEBUG:NONE
PROTOBUF_DIR=C:\Users\eriko\OneDrive\Documents\GitHub\vcpkg\packages\protobuf_x64-windows-static
INC_DIR = $(PROTOBUF_DIR)\include
LIB_DIR = $(PROTOBUF_DIR)\lib

all: lex2

lex2 : lex2.obj regex.obj Cache.pb.obj
	$(CPP) lex2.obj regex.obj Cache.pb.obj $(LIB_DIR)\libprotobuf.lib
lex2.obj : lex2.cpp lex2.h
	$(CPP) /c lex2.cpp /I$(INC_DIR)
regex.obj : regex.cpp regex.h
	$(CPP) /c regex.cpp /I$(INC_DIR)
Cache.pb.obj: Cache.pb.cc Cache.pb.h
	$(CPP) /c Cache.pb.cc /I$(INC_DIR)

clean:
	del *.obj lex2 cache\*