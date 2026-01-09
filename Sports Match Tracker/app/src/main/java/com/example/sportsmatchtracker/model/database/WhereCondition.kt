package com.example.sportsmatchtracker.model.database

data class WhereCondition(
    val column: String,
    val operator: String,
    val value: Any,
    val logicalOperator: LogicalOperator = LogicalOperator.AND
)
