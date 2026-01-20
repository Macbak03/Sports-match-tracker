package com.example.sportsmatchtracker.repository

import com.example.sportsmatchtracker.model.database.JoinClause
import com.example.sportsmatchtracker.model.database.WhereCondition
import com.example.sportsmatchtracker.network.SocketManager
import org.json.JSONArray
import org.json.JSONObject

open class Repository {
    protected val socketManager = SocketManager.getInstance()
    
    companion object {
        var onConnectionLost: (() -> Unit)? = null
    }
    
    protected suspend fun <T> withConnectionCheck(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            if (e.message?.contains("No response from server") == true) {
                onConnectionLost?.invoke()
            }
            throw e
        }
    }

    protected fun selectRequest(
        table: String,
        columns: List<String>,
        where: List<WhereCondition>? = null,
        orderBy: String? = null,
        orderDirection: String? = null,
        limit: Int? = null
    ): JSONObject {
        return JSONObject().apply {
            put("action", "SELECT")
            put("table", table)
            put("columns", JSONArray(columns))

            where?.let {
                put("where", JSONArray().apply {
                    it.forEach { condition ->
                        put(JSONObject().apply {
                            put("column", condition.column)
                            put("operator", condition.operator)
                            put("value", condition.value)
                            put("logical_operator", condition.logicalOperator.name)
                            condition.group?.let { group -> put("group", group) }
                        })
                    }
                })
            }

            orderBy?.let {
                put("order_by", it)
                put("order_direction", orderDirection ?: "ASC")
            }

            limit?.let {
                put("limit", it)
            }
        }
    }

    protected fun insertRequest(
        table: String,
        columns: List<String>,
        values: List<Any>
    ): JSONObject {
        return JSONObject().apply {
            put("action", "INSERT")
            put("table", table)
            put("columns", JSONArray(columns))
            put("values", JSONArray(values))
        }
    }

    protected fun updateRequest(
        table: String,
        columns: List<String>,
        values: List<Any>,
        where: List<WhereCondition>? = null
    ): JSONObject {
        return JSONObject().apply {
            put("action", "UPDATE")
            put("table", table)
            put("columns", JSONArray(columns))
            put("values", JSONArray(values))

            where?.let {
                put("where", JSONArray().apply {
                    it.forEach { condition ->
                        put(JSONObject().apply {
                            put("column", condition.column)
                            put("operator", condition.operator)
                            put("value", condition.value)
                            put("logical_operator", condition.logicalOperator.name)
                        })
                    }
                })
            }
        }
    }

    protected fun deleteRequest(
        table: String,
        where: List<WhereCondition>? = null
    ): JSONObject {
        return JSONObject().apply {
            put("action", "DELETE")
            put("table", table)
            where?.let {
                put("where", JSONArray().apply {
                    it.forEach { condition ->
                        put(JSONObject().apply {
                            put("column", condition.column)
                            put("operator", condition.operator)
                            put("value", condition.value)
                            put("logical_operator", condition.logicalOperator.name)
                        })
                    }
                })
            }
        }
    }

    protected fun selectWithJoinRequest(
        table: String,
        columns: List<String>,
        joins: List<JoinClause>,
        where: List<WhereCondition>? = null,
        orderBy: String? = null,
        orderDirection: String? = null,
        limit: Int? = null
    ): JSONObject {
        return JSONObject().apply {
            put("action", "SELECT")
            put("table", table)
            put("columns", JSONArray(columns))

            put("joins", JSONArray().apply {
                joins.forEach { join ->
                    put(JSONObject().apply {
                        put("table", join.table)
                        put("type", join.type.name)
                        put("on_left", join.onLeft)
                        put("on_right", join.onRight)
                    })
                }
            })

            where?.let {
                put("where", JSONArray().apply {
                    it.forEach { condition ->
                        put(JSONObject().apply {
                            put("column", condition.column)
                            put("operator", condition.operator)
                            put("value", condition.value)
                            put("logical_operator", condition.logicalOperator.name)
                            condition.group?.let { group -> put("group", group) }
                        })
                    }
                })
            }

            orderBy?.let {
                put("order_by", it)
                put("order_direction", orderDirection ?: "ASC")
            }

            limit?.let {
                put("limit", it)
            }
        }
    }
}