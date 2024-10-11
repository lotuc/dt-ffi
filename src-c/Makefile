ROOT_DIR := $(shell dirname $(realpath $(firstword $(MAKEFILE_LIST))))
DESTDIR  := $(ROOT_DIR)/../target/native

CC     = gcc
CFLAGS = -L$(DESTDIR) -I$(DESTDIR)

LIB_COMMON = $(DESTDIR)/libCommon.dylib
LIB_LIB1   = $(DESTDIR)/libLib1.dylib
EXE_FROM_C = $(DESTDIR)/from_c
OBJS = $(LIB_COMMON) $(LIB_LIB1) $(EXE_FROM_C)

all: $(OBJS)

$(LIB_COMMON): common.c
	$(CC) -o $@ $(CFLAGS) -fPIC -shared common.c

$(LIB_LIB1): lib1.c $(LIB_COMMON)
	$(CC) -o $@ $(CFLAGS) -fPIC -shared -lCommon lib1.c

$(EXE_FROM_C): from_c.c $(LIB_COMMON)
	$(CC) -o $@ $(CFLAGS) -lsci -lCommon from_c.c

clean:
	rm -f $(DESTDIR)/libCommon.dylib $(DESTDIR)/libLib1.dylib
