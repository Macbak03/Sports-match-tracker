#include "cJSON.h"
#include <arpa/inet.h>
#include <fcntl.h> // for open
#include <netinet/in.h>
#include <oci.h>
#include <pthread.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <time.h>
#include <unistd.h> // for close
pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;

volatile sig_atomic_t keep_running = 1;
int serverSocket = -1;

OCIEnv *env;
OCIError *err;
OCISvcCtx *svc;
OCIStmt *stmt = NULL;
OCIDefine *def = NULL;

// Execute SELECT query and return results as JSON
int executeDynamicSelect(cJSON *json, char *response) {
  sword rc;
  char sql[2000];
  char email[256], nick[101], haslo[256];

  // Get table name
  cJSON *table = cJSON_GetObjectItem(json, "table");
  if (!cJSON_IsString(table)) {
    sprintf(response,
            "{\"status\":\"error\",\"message\":\"Missing table name\"}");
    return 1;
  }

  // Build SELECT columns
  cJSON *columns = cJSON_GetObjectItem(json, "columns");
  char columns_str[500] = "*";
  if (cJSON_IsArray(columns)) {
    columns_str[0] = '\0';
    int size = cJSON_GetArraySize(columns);
    for (int i = 0; i < size; i++) {
      cJSON *col = cJSON_GetArrayItem(columns, i);
      if (cJSON_IsString(col)) {
        strcat(columns_str, col->valuestring);
        if (i < size - 1)
          strcat(columns_str, ", ");
      }
    }
  }

  // Build WHERE clause
  char where_str[500] = "";
  cJSON *where = cJSON_GetObjectItem(json, "where");
  if (cJSON_IsObject(where)) {
    cJSON *column = cJSON_GetObjectItem(where, "column");
    cJSON *operator = cJSON_GetObjectItem(where, "operator");
    cJSON *value = cJSON_GetObjectItem(where, "value");

    if (cJSON_IsString(column) && cJSON_IsString(operator) &&
        cJSON_IsString(value)) {
      sprintf(where_str, " WHERE %s %s '%s'", column->valuestring,
              operator->valuestring, value->valuestring);
    }
  }

  // Build final SQL
  sprintf(sql, "SELECT %s FROM %s%s", columns_str, table->valuestring,
          where_str);
  printf("Executing SQL: %s\n", sql);

  // Prepare and execute
  OCIStmt *query_stmt = NULL;
  OCIHandleAlloc(env, (void **)&query_stmt, OCI_HTYPE_STMT, 0, NULL);
  OCIStmtPrepare(query_stmt, err, (OraText *)sql, strlen(sql), OCI_NTV_SYNTAX,
                 OCI_DEFAULT);

  // Define output variables
  OCIDefine *def1 = NULL, *def2 = NULL, *def3 = NULL;
  OCIDefineByPos(query_stmt, &def1, err, 1, email, sizeof(email), SQLT_STR,
                 NULL, NULL, NULL, OCI_DEFAULT);
  OCIDefineByPos(query_stmt, &def2, err, 2, nick, sizeof(nick), SQLT_STR, NULL,
                 NULL, NULL, OCI_DEFAULT);
  OCIDefineByPos(query_stmt, &def3, err, 3, haslo, sizeof(haslo), SQLT_STR,
                 NULL, NULL, NULL, OCI_DEFAULT);

  rc = OCIStmtExecute(svc, query_stmt, err, 0, 0, NULL, NULL, OCI_DEFAULT);
  if (rc != OCI_SUCCESS && rc != OCI_SUCCESS_WITH_INFO) {
    text errbuf[512];
    sb4 errcode;
    OCIErrorGet(err, 1, NULL, &errcode, errbuf, sizeof(errbuf),
                OCI_HTYPE_ERROR);
    sprintf(response, "{\"status\":\"error\",\"message\":\"Query failed: %s\"}",
            errbuf);
    OCIHandleFree(query_stmt, OCI_HTYPE_STMT);
    return 1;
  }

  // Build JSON response with results
  cJSON *json_response = cJSON_CreateObject();
  cJSON_AddStringToObject(json_response, "status", "success");
  cJSON *data_array = cJSON_CreateArray();

  // Fetch all rows
  while (OCIStmtFetch2(query_stmt, err, 1, OCI_FETCH_NEXT, 0, OCI_DEFAULT) ==
         OCI_SUCCESS) {
    cJSON *row = cJSON_CreateObject();
    cJSON_AddStringToObject(row, "email", email);
    cJSON_AddStringToObject(row, "nick", nick);
    cJSON_AddStringToObject(row, "haslo", haslo);
    cJSON_AddItemToArray(data_array, row);
  }

  cJSON_AddItemToObject(json_response, "data", data_array);
  cJSON_AddNumberToObject(json_response, "count",
                          cJSON_GetArraySize(data_array));

  char *json_str = cJSON_PrintUnformatted(json_response);
  strcpy(response, json_str);
  free(json_str);
  cJSON_Delete(json_response);

  OCIHandleFree(query_stmt, OCI_HTYPE_STMT);
  return 0;
}

// Parse JSON recived from client and send a response
void process_sql_request(char *json_message, char *response) {
  cJSON *json = cJSON_Parse(json_message);

  if (json == NULL) {
    const char *error_ptr = cJSON_GetErrorPtr();
    sprintf(response, "{\"status\":\"error\",\"message\":\"Invalid JSON: %s\"}",
            error_ptr ? error_ptr : "unknown error");
    printf("Failed to parse JSON\n");
    return;
  }

  // Get action type
  cJSON *action = cJSON_GetObjectItem(json, "action");

  if (!cJSON_IsString(action)) {
    sprintf(response,
            "{\"status\":\"error\",\"message\":\"Missing action field\"}");
    cJSON_Delete(json);
    return;
  }

  // Execute appropriate query based on action
  if (strcmp(action->valuestring, "SELECT") == 0) {
    executeDynamicSelect(json, response);
  } else {
    sprintf(response,
            "{\"status\":\"error\",\"message\":\"Unsupported action: %s\"}",
            action->valuestring);
  }

  cJSON_Delete(json);
}

void handle_sigint(int sig) {
  printf("\nReceived signal %d, shutting down...\n", sig);
  keep_running = 0;
  if (serverSocket != -1) {
    shutdown(serverSocket, SHUT_RDWR);
    close(serverSocket);
  }
}

int initOCI() {
  sword rc;

  rc = OCIEnvCreate(&env, OCI_DEFAULT, NULL, NULL, NULL, NULL, 0, NULL);
  if (rc != OCI_SUCCESS) {
    printf("OCIEnvCreate failed\n");
    return 1;
  }
  OCIHandleAlloc(env, (void **)&err, OCI_HTYPE_ERROR, 0, NULL);
  return 0;
}

int connectToOracle() {
  sword rc;

  const char *db_user = "server";
  const char *db_pass = "ServerPassword";
  const char *db_conn = "//172.18.16.1:1521/orcl1.db.com";
  rc = OCILogon(env, err, &svc, (OraText *)db_user, strlen(db_user),
                (OraText *)db_pass, strlen(db_pass), (OraText *)db_conn,
                strlen(db_conn));

  if (rc != OCI_SUCCESS) {
    text errbuf[512];
    sb4 errcode;
    OCIErrorGet(err, 1, NULL, &errcode, errbuf, sizeof(errbuf),
                OCI_HTYPE_ERROR);
    printf("Login failed: %s\n", errbuf);
    return 1;
  }

  printf("Connected to Oracle OK\n");
  return 0;
}

int executeSelect() {
  sword rc;
  char email[256];
  char nick[101];
  char haslo[256];
  const char *sql = "SELECT email, nick, haslo FROM UZYTKOWNIK";

  OCIHandleAlloc(env, (void **)&stmt, OCI_HTYPE_STMT, 0, NULL);
  OCIStmtPrepare(stmt, err, (OraText *)sql, strlen(sql), OCI_NTV_SYNTAX,
                 OCI_DEFAULT);

  // Define output variables for each column
  OCIDefine *def1 = NULL, *def2 = NULL, *def3 = NULL;
  OCIDefineByPos(stmt, &def1, err, 1, email, sizeof(email), SQLT_STR, NULL,
                 NULL, NULL, OCI_DEFAULT);
  OCIDefineByPos(stmt, &def2, err, 2, nick, sizeof(nick), SQLT_STR, NULL, NULL,
                 NULL, OCI_DEFAULT);
  OCIDefineByPos(stmt, &def3, err, 3, haslo, sizeof(haslo), SQLT_STR, NULL,
                 NULL, NULL, OCI_DEFAULT);

  rc = OCIStmtExecute(svc, stmt, err, 0, 0, NULL, NULL, OCI_DEFAULT);
  if (rc != OCI_SUCCESS && rc != OCI_SUCCESS_WITH_INFO) {
    text errbuf[512];
    sb4 errcode;
    OCIErrorGet(err, 1, NULL, &errcode, errbuf, sizeof(errbuf),
                OCI_HTYPE_ERROR);
    printf("Execute failed: %s\n", errbuf);
    return 1;
  }

  printf("Results from UZYTKOWNIK table:\n");
  printf("%-30s %-20s %-30s\n", "EMAIL", "NICK", "HASLO");
  printf(
      "-------------------------------------------------------------------\n");

  // Fetch all rows
  while (OCIStmtFetch2(stmt, err, 1, OCI_FETCH_NEXT, 0, OCI_DEFAULT) ==
         OCI_SUCCESS) {
    printf("%-30s %-20s %-30s\n", email, nick, haslo);
  }

  OCIHandleFree(stmt, OCI_HTYPE_STMT);
  return 0;
}

void *socketThread(void *arg) {
  int newSocket = *((int *)arg);
  char client_message[2000];
  char response[2000];

  while (1) {
    int n = recv(newSocket, client_message, sizeof(client_message) - 1, 0);
    if (n <= 0) {
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

int main() {
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
  if (oci == 1) {
    return 1;
  }
  int connection = connectToOracle();
  if (connection == 1) {
    return 1;
  }

  int query = executeSelect();
  if (query == 1) {
    return 1;
  }

  // Listen on the socket
  if (listen(serverSocket, 50) == 0)
    printf("Listening\n");
  else
    printf("Error\n");

  pthread_t thread_id;

  while (keep_running) {
    // Accept call creates a new socket for the incoming connection
    addr_size = sizeof serverStorage;
    newSocket =
        accept(serverSocket, (struct sockaddr *)&serverStorage, &addr_size);

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
