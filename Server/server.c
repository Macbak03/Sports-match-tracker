#include "cJSON.h"
#include <arpa/inet.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <pthread.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <time.h>
#include <unistd.h>
#include <postgresql/libpq-fe.h>

pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;

volatile sig_atomic_t keep_running = 1;
int serverSocket = -1;

/* PostgreSQL connection */
PGconn *conn = NULL;

/* ===================== SELECT DYNAMIC ===================== */
int executeDynamicSelect(cJSON *json, char *response) {
    char sql[2048];

    cJSON *table = cJSON_GetObjectItem(json, "table");
    if (!cJSON_IsString(table)) {
        sprintf(response, "{\"status\":\"error\",\"message\":\"Missing table name\"}");
        return 1;
    }

    /* Columns */
    char columns_str[512] = "*";
    cJSON *columns = cJSON_GetObjectItem(json, "columns");
    if (cJSON_IsArray(columns)) {
        columns_str[0] = '\0';
        int size = cJSON_GetArraySize(columns);
        for (int i = 0; i < size; i++) {
            cJSON *col = cJSON_GetArrayItem(columns, i);
            if (cJSON_IsString(col)) {
                strcat(columns_str, col->valuestring);
                if (i < size - 1) strcat(columns_str, ", ");
            }
        }
    }

    /* WHERE */
    char where_str[512] = "";
    cJSON *where = cJSON_GetObjectItem(json, "where");
    if (cJSON_IsObject(where)) {
        cJSON *column = cJSON_GetObjectItem(where, "column");
        cJSON *op = cJSON_GetObjectItem(where, "operator");
        cJSON *value = cJSON_GetObjectItem(where, "value");

        if (cJSON_IsString(column) && cJSON_IsString(op) && cJSON_IsString(value)) {
            sprintf(where_str, " WHERE %s %s '%s'",
                    column->valuestring,
                    op->valuestring,
                    value->valuestring);
        }
    }

    sprintf(sql, "SELECT %s FROM %s%s", columns_str, table->valuestring, where_str);
    printf("Executing SQL: %s\n", sql);

    PGresult *res = PQexec(conn, sql);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        sprintf(response,
                "{\"status\":\"error\",\"message\":\"Query failed: %s\"}",
                PQerrorMessage(conn));
        PQclear(res);
        return 1;
    }

    int rows = PQntuples(res);

    cJSON *json_response = cJSON_CreateObject();
    cJSON_AddStringToObject(json_response, "status", "success");
    cJSON *data = cJSON_CreateArray();

    for (int i = 0; i < rows; i++) {
        cJSON *row = cJSON_CreateObject();
        cJSON_AddStringToObject(row, "email", PQgetvalue(res, i, 0));
        cJSON_AddStringToObject(row, "nick", PQgetvalue(res, i, 1));
        cJSON_AddStringToObject(row, "haslo", PQgetvalue(res, i, 2));
        cJSON_AddItemToArray(data, row);
    }

    cJSON_AddItemToObject(json_response, "data", data);
    cJSON_AddNumberToObject(json_response, "count", rows);

    char *out = cJSON_PrintUnformatted(json_response);
    strcpy(response, out);

    free(out);
    cJSON_Delete(json_response);
    PQclear(res);

    return 0;
}

/* ===================== JSON ROUTER ===================== */
void process_sql_request(char *json_message, char *response) {
    cJSON *json = cJSON_Parse(json_message);
    if (!json) {
        sprintf(response, "{\"status\":\"error\",\"message\":\"Invalid JSON\"}");
        return;
    }

    cJSON *action = cJSON_GetObjectItem(json, "action");
    if (!cJSON_IsString(action)) {
        sprintf(response, "{\"status\":\"error\",\"message\":\"Missing action\"}");
        cJSON_Delete(json);
        return;
    }

    if (strcmp(action->valuestring, "SELECT") == 0) {
        executeDynamicSelect(json, response);
    } else {
        sprintf(response,
                "{\"status\":\"error\",\"message\":\"Unsupported action\"}");
    }

    cJSON_Delete(json);
}

/* ===================== SIGNAL ===================== */
void handle_sigint(int sig) {
    keep_running = 0;
    if (serverSocket != -1) {
        shutdown(serverSocket, SHUT_RDWR);
        close(serverSocket);
    }
}

/* ===================== POSTGRES ===================== */
int connectToPostgres() {
    conn = PQconnectdb(
        "host=127.0.0.1 port=5432 dbname=database user=server password=ServerPassword"
    );

    if (PQstatus(conn) != CONNECTION_OK) {
        printf("PostgreSQL connection failed: %s\n", PQerrorMessage(conn));
        return 1;
    }

    printf("Connected to PostgreSQL OK\n");
    return 0;
}

/* ===================== SOCKET THREAD ===================== */
void *socketThread(void *arg) {
    int sock = *((int *)arg);
    char buffer[2048];
    char response[2048];

    while (1) {
        int n = recv(sock, buffer, sizeof(buffer) - 1, 0);
        if (n <= 0) break;

        buffer[n] = '\0';
        printf("Received JSON: %s\n", buffer);

        process_sql_request(buffer, response);

        send(sock, response, strlen(response), 0);
        send(sock, "\n", 1, 0);
    }

    close(sock);
    pthread_exit(NULL);
}

/* ===================== MAIN ===================== */
int main() {
    struct sockaddr_in serverAddr;
    struct sockaddr_storage serverStorage;
    socklen_t addr_size;

    serverSocket = socket(AF_INET, SOCK_STREAM, 0);

    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(1100);
    serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);
    memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);

    bind(serverSocket, (struct sockaddr *)&serverAddr, sizeof(serverAddr));
    listen(serverSocket, 50);

    signal(SIGINT, handle_sigint);

    if (connectToPostgres() != 0) return 1;

    printf("Listening...\n");

    while (keep_running) {
        addr_size = sizeof serverStorage;
        int newSocket =
            accept(serverSocket, (struct sockaddr *)&serverStorage, &addr_size);
        if (newSocket < 0) break;

        pthread_t tid;
        pthread_create(&tid, NULL, socketThread, &newSocket);
        pthread_detach(tid);
    }

    if (conn) PQfinish(conn);
    close(serverSocket);
    printf("Server stopped.\n");

    return 0;
}
