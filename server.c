#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>
#include <fcntl.h>  // for open
#include <unistd.h> // for close
#include <pthread.h>
#include <time.h>
#include "cJSON.h"
pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;

// Parse JSON recived from client and send a response
void process_sql_request(char *json_message, char *response)
{
  cJSON *json = cJSON_Parse(json_message);

  if (json == NULL)
  {
    const char *error_ptr = cJSON_GetErrorPtr();
    sprintf(response, "{\"status\":\"error\",\"message\":\"Invalid JSON: %s\"}",
            error_ptr ? error_ptr : "unknown error");
    printf("Failed to parse JSON\n");
    return;
  }

  // Get JSON objects
  cJSON *type = cJSON_GetObjectItem(json, "type");
  cJSON *action = cJSON_GetObjectItem(json, "action");
  cJSON *table = cJSON_GetObjectItem(json, "table");

  if (cJSON_IsString(type) && cJSON_IsString(action))
  {
    printf("Parsed SQL Request:\n");
    printf("  Type: %s\n", type->valuestring);
    printf("  Action: %s\n", action->valuestring);

    if (cJSON_IsString(table))
    {
      printf("  Table: %s\n", table->valuestring);
    }

    // Columns
    cJSON *columns = cJSON_GetObjectItem(json, "columns");
    if (cJSON_IsArray(columns))
    {
      printf("  Columns: ");
      int size = cJSON_GetArraySize(columns);
      for (int i = 0; i < size; i++)
      {
        cJSON *column = cJSON_GetArrayItem(columns, i);
        if (cJSON_IsString(column))
        {
          printf("%s%s", column->valuestring, (i < size - 1) ? ", " : "");
        }
      }
      printf("\n");
    }

    // Where conditions
    cJSON *where = cJSON_GetObjectItem(json, "where");
    if (cJSON_IsObject(where))
    {
      cJSON *column = cJSON_GetObjectItem(where, "column");
      cJSON *operator = cJSON_GetObjectItem(where, "operator");
      cJSON *value = cJSON_GetObjectItem(where, "value");

      if (cJSON_IsString(column) && cJSON_IsString(operator) && cJSON_IsString(value))
      {
        printf("  WHERE: %s %s '%s'\n", column->valuestring, operator->valuestring, value->valuestring);
      }
    }

    // Repsonse
    sprintf(response, "{\"status\":\"success\",\"message\":\"SQL %s request received and queued for table '%s'\",\"timestamp\":%ld}",
            action->valuestring,
            cJSON_IsString(table) ? table->valuestring : "unknown",
            time(NULL));
  }
  else
  {
    sprintf(response, "{\"status\":\"error\",\"message\":\"Missing required fields: type and action\"}");
    printf("Invalid JSON structure - missing type or action\n");
  }

  cJSON_Delete(json);
}

void *socketThread(void *arg)
{
  int newSocket = *((int *)arg);
  char client_message[2000];
  char response[2000];

  while (1)
  {
    int n = recv(newSocket, client_message, sizeof(client_message) - 1, 0);
    if (n <= 0)
    {
      break;
    }

    client_message[n] = '\0';
    printf("Received JSON: %s\n", client_message);

    process_sql_request(client_message, response);

    printf("Sending response: %s\n", response);
    send(newSocket, response, strlen(response), 0);
    send(newSocket, "\n", 1, 0);
  }

  close(newSocket);
  pthread_exit(NULL);
}

int main()
{
  int serverSocket, newSocket;
  struct sockaddr_in serverAddr;
  struct sockaddr_storage serverStorage;
  socklen_t addr_size;

  // Create the socket.
  serverSocket = socket(PF_INET, SOCK_STREAM, 0);

  // Configure settings of the server address struct
  // Address family = Internet
  serverAddr.sin_family = AF_INET;

  // Set port number, using htons function to use proper byte order
  serverAddr.sin_port = htons(1100);

  // Set IP address to localhost
  serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);

  // Set all bits of the padding field to 0
  memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);

  // Bind the address struct to the socket
  bind(serverSocket, (struct sockaddr *)&serverAddr, sizeof(serverAddr));

  // Listen on the socket
  if (listen(serverSocket, 50) == 0)
    printf("Listening\n");
  else
    printf("Error\n");

  pthread_t thread_id;

  while (1)
  {
    // Accept call creates a new socket for the incoming connection
    addr_size = sizeof serverStorage;
    newSocket = accept(serverSocket, (struct sockaddr *)&serverStorage, &addr_size);

    if (pthread_create(&thread_id, NULL, socketThread, &newSocket) != 0)
      printf("Failed to create thread\n");

    pthread_detach(thread_id);
    // pthread_join(thread_id,NULL);
  }
  return 0;
}