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
#include <sqlite3.h>

pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;

volatile sig_atomic_t keep_running = 1;
int serverSocket = -1;

/* SQLite connection */
sqlite3 *db = NULL;

/* ===================== SELECT DYNAMIC ===================== */
int executeDynamicSelect(cJSON *json, char *response)
{
    char sql[8192]; // Zwiększony rozmiar dla bezpieczeństwa

    cJSON *table = cJSON_GetObjectItem(json, "table");
    if (!cJSON_IsString(table))
    {
        sprintf(response, "{\"status\":\"error\",\"message\":\"Missing table name\"}");
        return 1;
    }

    /* Columns */
    char columns_str[2048] = "*";
    cJSON *columns = cJSON_GetObjectItem(json, "columns");
    if (cJSON_IsArray(columns))
    {
        columns_str[0] = '\0';
        int size = cJSON_GetArraySize(columns);
        for (int i = 0; i < size; i++)
        {
            cJSON *col = cJSON_GetArrayItem(columns, i);
            if (cJSON_IsString(col))
            {
                strcat(columns_str, col->valuestring);
                if (i < size - 1)
                    strcat(columns_str, ", ");
            }
        }
    }

    /* JOINs */
    char joins_str[3072] = "";
    cJSON *joins = cJSON_GetObjectItem(json, "joins");
    if (cJSON_IsArray(joins))
    {
        int join_count = cJSON_GetArraySize(joins);
        for (int i = 0; i < join_count; i++)
        {
            cJSON *join = cJSON_GetArrayItem(joins, i);
            cJSON *join_table = cJSON_GetObjectItem(join, "table");
            cJSON *join_type = cJSON_GetObjectItem(join, "type");
            cJSON *on_left = cJSON_GetObjectItem(join, "on_left");
            cJSON *on_right = cJSON_GetObjectItem(join, "on_right");

            if (cJSON_IsString(join_table) && cJSON_IsString(join_type) &&
                cJSON_IsString(on_left) && cJSON_IsString(on_right))
            {
                char join_clause[512];
                sprintf(join_clause, " %s JOIN %s ON %s = %s",
                        join_type->valuestring,
                        join_table->valuestring,
                        on_left->valuestring,
                        on_right->valuestring);
                strcat(joins_str, join_clause);
            }
        }
    }

    /* WHERE */
    char where_str[2048] = "";
    cJSON *where = cJSON_GetObjectItem(json, "where");
    if (cJSON_IsObject(where))
    {
        cJSON *column = cJSON_GetObjectItem(where, "column");
        cJSON *op = cJSON_GetObjectItem(where, "operator");
        cJSON *value = cJSON_GetObjectItem(where, "value");

        if (cJSON_IsString(column) && cJSON_IsString(op) && cJSON_IsString(value))
        {
            sprintf(where_str, " WHERE %s %s '%s'",
                    column->valuestring,
                    op->valuestring,
                    value->valuestring);
        }
    }
    else if (cJSON_IsArray(where))
    {
        /* Multiple WHERE conditions with AND */
        int where_count = cJSON_GetArraySize(where);
        if (where_count > 0)
        {
            strcat(where_str, " WHERE ");
            for (int i = 0; i < where_count; i++)
            {
                cJSON *condition = cJSON_GetArrayItem(where, i);
                cJSON *column = cJSON_GetObjectItem(condition, "column");
                cJSON *op = cJSON_GetObjectItem(condition, "operator");
                cJSON *value = cJSON_GetObjectItem(condition, "value");

                if (cJSON_IsString(column) && cJSON_IsString(op) && cJSON_IsString(value))
                {
                    char condition_str[256];
                    sprintf(condition_str, "%s %s '%s'",
                            column->valuestring,
                            op->valuestring,
                            value->valuestring);
                    strcat(where_str, condition_str);

                    if (i < where_count - 1)
                        strcat(where_str, " AND ");
                }
            }
        }
    }

    snprintf(sql, sizeof(sql), "SELECT %s FROM %s%s%s",
             columns_str, table->valuestring, joins_str, where_str);
    printf("Executing SQL: %s\n", sql);

    sqlite3_stmt *stmt;
    int rc = sqlite3_prepare_v2(db, sql, -1, &stmt, NULL);

    if (rc != SQLITE_OK)
    {
        sprintf(response,
                "{\"status\":\"error\",\"message\":\"Query failed: %s\"}",
                sqlite3_errmsg(db));
        return 1;
    }

    cJSON *json_response = cJSON_CreateObject();
    cJSON_AddStringToObject(json_response, "status", "success");
    cJSON *data = cJSON_CreateArray();

    int rows = 0;
    while (sqlite3_step(stmt) == SQLITE_ROW)
    {
        cJSON *row = cJSON_CreateObject();
        int cols = sqlite3_column_count(stmt);

        for (int i = 0; i < cols; i++)
        {
            const char *col_name = sqlite3_column_name(stmt, i);
            const char *col_value = (const char *)sqlite3_column_text(stmt, i);
            cJSON_AddStringToObject(row, col_name, col_value ? col_value : "");
        }

        cJSON_AddItemToArray(data, row);
        rows++;
    }

    cJSON_AddItemToObject(json_response, "data", data);
    cJSON_AddNumberToObject(json_response, "count", rows);

    char *out = cJSON_PrintUnformatted(json_response);
    strcpy(response, out);
    printf("Response JSON: %s\n", response);

    free(out);
    cJSON_Delete(json_response);
    sqlite3_finalize(stmt);

    return 0;
}

/* ===================== INSERT DYNAMIC ===================== */
int executeDynamicInsert(cJSON *json, char *response)
{
    char sql[4096]; // Zwiększony rozmiar

    cJSON *table = cJSON_GetObjectItem(json, "table");
    if (!cJSON_IsString(table))
    {
        sprintf(response, "{\"status\":\"error\",\"message\":\"Missing table name\"}");
        return 1;
    }

    /* Columns */
    cJSON *columns = cJSON_GetObjectItem(json, "columns");
    if (!cJSON_IsArray(columns))
    {
        sprintf(response, "{\"status\":\"error\",\"message\":\"Missing columns array\"}");
        return 1;
    }

    /* Values */
    cJSON *values = cJSON_GetObjectItem(json, "values");
    if (!cJSON_IsArray(values))
    {
        sprintf(response, "{\"status\":\"error\",\"message\":\"Missing values array\"}");
        return 1;
    }

    int col_count = cJSON_GetArraySize(columns);
    int val_count = cJSON_GetArraySize(values);

    if (col_count != val_count)
    {
        sprintf(response, "{\"status\":\"error\",\"message\":\"Column and value count mismatch\"}");
        return 1;
    }

    /* Build columns string */
    char columns_str[1024] = "";
    for (int i = 0; i < col_count; i++)
    {
        cJSON *col = cJSON_GetArrayItem(columns, i);
        if (cJSON_IsString(col))
        {
            strcat(columns_str, col->valuestring);
            if (i < col_count - 1)
                strcat(columns_str, ", ");
        }
    }

    /* Build values string */
    char values_str[1024] = "";
    for (int i = 0; i < val_count; i++)
    {
        cJSON *val = cJSON_GetArrayItem(values, i);
        if (cJSON_IsString(val))
        {
            strcat(values_str, "'");
            strcat(values_str, val->valuestring);
            strcat(values_str, "'");
            if (i < val_count - 1)
                strcat(values_str, ", ");
        }
    }

    snprintf(sql, sizeof(sql), "INSERT INTO %s (%s) VALUES (%s)",
             table->valuestring, columns_str, values_str);
    printf("Executing SQL: %s\n", sql);

    char *errMsg = NULL;
    int rc = sqlite3_exec(db, sql, NULL, NULL, &errMsg);

    if (rc != SQLITE_OK)
    {
        sprintf(response,
                "{\"status\":\"error\",\"message\":\"Insert failed: %s\"}",
                errMsg ? errMsg : sqlite3_errmsg(db));
        if (errMsg)
            sqlite3_free(errMsg);
        return 1;
    }

    long long lastId = sqlite3_last_insert_rowid(db);

    sprintf(response,
            "{\"status\":\"success\",\"message\":\"Row inserted\",\"lastId\":%lld}",
            lastId);
    printf("Response JSON: %s\n", response);
    return 0;
}

/* ===================== JSON ROUTER ===================== */
void process_sql_request(char *json_message, char *response)
{
    cJSON *json = cJSON_Parse(json_message);
    if (!json)
    {
        sprintf(response, "{\"status\":\"error\",\"message\":\"Invalid JSON\"}");
        return;
    }

    cJSON *action = cJSON_GetObjectItem(json, "action");
    if (!cJSON_IsString(action))
    {
        sprintf(response, "{\"status\":\"error\",\"message\":\"Missing action\"}");
        cJSON_Delete(json);
        return;
    }

    if (strcmp(action->valuestring, "SELECT") == 0)
    {
        executeDynamicSelect(json, response);
    }
    else if (strcmp(action->valuestring, "INSERT") == 0)
    {
        executeDynamicInsert(json, response);
    }
    else
    {
        sprintf(response,
                "{\"status\":\"error\",\"message\":\"Unsupported action\"}");
    }

    cJSON_Delete(json);
}

/* ===================== SIGNAL ===================== */
void handle_sigint(int sig)
{
    keep_running = 0;
    if (serverSocket != -1)
    {
        shutdown(serverSocket, SHUT_RDWR);
        close(serverSocket);
    }
}

/* ===================== SQLITE ===================== */
int connectToSQLite()
{
    int rc = sqlite3_open("../database/sports.db", &db);

    if (rc != SQLITE_OK)
    {
        printf("SQLite connection failed: %s\n", sqlite3_errmsg(db));
        return 1;
    }

    printf("Connected to SQLite OK\n");
    return 0;
}

/* ===================== SOCKET THREAD ===================== */
void *socketThread(void *arg)
{
    int sock = *((int *)arg);
    printf("Client connected: socket %d\n", sock);
    if (sock >= 0)
    {
        char *connected = "connected\n";
        send(sock, connected, strlen(connected), 0);
    }
    char buffer[8192];   // Zwiększony dla większych zapytań
    char response[8192]; // Zwiększony dla większych odpowiedzi

    while (1)
    {
        int n = recv(sock, buffer, sizeof(buffer) - 1, 0);
        if (n <= 0)
            break;

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
int main()
{
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

    if (connectToSQLite() != 0)
        return 1;

    printf("Listening...\n");

    while (keep_running)
    {
        addr_size = sizeof serverStorage;
        int newSocket =
            accept(serverSocket, (struct sockaddr *)&serverStorage, &addr_size);
        if (newSocket < 0)
            break;

        pthread_t tid;
        pthread_create(&tid, NULL, socketThread, &newSocket);
        pthread_detach(tid);
    }

    if (db)
        sqlite3_close(db);
    close(serverSocket);
    printf("Server stopped.\n");

    return 0;
}
