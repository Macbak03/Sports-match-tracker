CC = gcc

ORACLE_HOME ?= /c/Users/macie/Studia/SBD/Sports-match-tracker/oracle/instantclient_23_26
OCI_INC = $(ORACLE_HOME)/sdk/include
OCI_LIB = $(ORACLE_HOME)

CFLAGS = -Wall -pthread -I$(OCI_INC)
LDFLAGS = -lpthread -lclntsh -L$(OCI_LIB)

TARGET = server.out
SRCS = server.c cJSON.c
OBJS = $(SRCS:.c=.o)

$(TARGET): $(OBJS)
	$(CC) $(OBJS) -o $(TARGET) $(LDFLAGS)

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	rm -f $(OBJS) $(TARGET)

.PHONY: clean