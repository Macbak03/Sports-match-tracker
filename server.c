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
#include <signal.h>
#include "cJSON.h"
#include <oci.h>
pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;

volatile sig_atomic_t keep_running = 1;
int serverSocket = -1;

OCIEnv *env;
OCIError *err;
OCISvcCtx *svc;
OCIStmt *stmt = NULL;
OCIDefine *def = NULL;

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

void handle_sigint(int sig)
{
  printf("\nReceived signal %d, shutting down...\n", sig);
  keep_running = 0;
  if (serverSocket != -1)
  {
    shutdown(serverSocket, SHUT_RDWR);
    close(serverSocket);
  }
}

int initOCI()
{
  sword rc;

  rc = OCIEnvCreate(&env, OCI_DEFAULT, NULL, NULL, NULL, NULL, 0, NULL);
  if (rc != OCI_SUCCESS)
  {
    printf("OCIEnvCreate failed\n");
    return 1;
  }
  OCIHandleAlloc(env, (void **)&err, OCI_HTYPE_ERROR, 0, NULL);
  return 0;
}

int connectToOracle()
{
  sword rc;

  const char *db_user = "server";
  const char *db_pass = "ServerPassword";
  const char *db_conn = "//172.18.16.1:1521/orcl1.db.com";
  rc = OCILogon(
      env, err, &svc,
      (OraText *)db_user, strlen(db_user),
      (OraText *)db_pass, strlen(db_pass),
      (OraText *)db_conn, strlen(db_conn));

  if (rc != OCI_SUCCESS)
  {
    text errbuf[512];
    sb4 errcode;
    OCIErrorGet(err, 1, NULL, &errcode, errbuf, sizeof(errbuf), OCI_HTYPE_ERROR);
    printf("Login failed: %s\n", errbuf);
    return 1;
  }

  printf("Connected to Oracle OK\n");
  return 0;
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
  int newSocket;
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

  // Setup signal handler for Ctrl+C
  signal(SIGINT, handle_sigint);

  int oci = initOCI();
  if (oci == 1)
  {
    return 1;
  }
  int connection = connectToOracle();
  if (connection == 1)
  {
    return 1;
  }

  // Listen on the socket
  if (listen(serverSocket, 50) == 0)
    printf("Listening\n");
  else
    printf("Error\n");

  pthread_t thread_id;

  while (keep_running)
  {
    // Accept call creates a new socket for the incoming connection
    addr_size = sizeof serverStorage;
    newSocket = accept(serverSocket, (struct sockaddr *)&serverStorage, &addr_size);

    if (!keep_running || newSocket < 0)
      break;

    if (pthread_create(&thread_id, NULL, socketThread, &newSocket) != 0)
      printf("Failed to create thread\n");

    pthread_detach(thread_id);
    // pthread_join(thread_id,NULL);
  }

  // Cleanup
  printf("Closing server socket and disconnecting from Oracle...\n");
  if (serverSocket != -1)
    close(serverSocket);
  OCILogoff(svc, err);
  OCIHandleFree(err, OCI_HTYPE_ERROR);
  OCIHandleFree(env, OCI_HTYPE_ENV);
  printf("Server stopped.\n");

  return 0;
}
