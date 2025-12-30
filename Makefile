CC=gcc
CFLAGS=-Wall -pthread
OBJS=server.o cJSON.o

server: $(OBJS)
	$(CC) $(CFLAGS) -o server $(OBJS) -lm

server.o: server.c cJSON.h
	$(CC) $(CFLAGS) -c server.c

cJSON.o: cJSON.c cJSON.h
	$(CC) $(CFLAGS) -c cJSON.c

clean:
	rm -f *.o server

.PHONY: clean
