CC = gcc

CFLAGS = -Wall -pthread -IServer
LDFLAGS = -lpthread -lsqlite3

TARGET = server.out
SRCS = server.c cJSON.c
OBJS = $(SRCS:.c=.o)

$(TARGET): $(OBJS)
	$(CC) $(OBJS) -o $(TARGET) $(LDFLAGS)

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	rm -f $(OBJS) $(TARGET)

init-db:
	sqlite3 ../database/sports.db < ../database/init.sql

.PHONY: clean init-db