package org.jetbrains.squash

import kotlinx.support.jdk7.*
import org.jetbrains.squash.expressions.*
import java.util.*

class Database(val connection: DatabaseConnection, val tables: List<Table>) {

    data class DatabaseSchemaValidationItem(val message: String)

    fun validateSchema(): List<DatabaseSchemaValidationItem> = connection.createTransaction().use { transaction ->
        val dialect = connection.dialect
        val tableMap = tables.associateBy { it.tableName.identifier.toLowerCase() }
        val validationResult = mutableListOf<DatabaseSchemaValidationItem>()
        transaction.querySchema().tables().forEach { tableSchema ->
            val tableDefinition = tableMap[tableSchema.name.toLowerCase()]
            if (tableDefinition == null)
                validationResult.add(DatabaseSchemaValidationItem("Table definition not found for schema table '$tableSchema"))
            else {
                val columnsSchema = tableSchema.columns().associateBy { it.name.toLowerCase() }
                val columnsDefinition = tableDefinition.tableColumns.associateBy { dialect.nameSQL(it.name.identifier).toLowerCase() }
                val allNames = columnsDefinition.keys + columnsSchema.keys
                for (name in allNames) {
                    val columnSchema = columnsSchema[name]
                    val columnDefinition = columnsDefinition[name]
                    when {
                        columnSchema == null -> validationResult.add(DatabaseSchemaValidationItem("Column schema not found for definition column '$columnDefinition in table '$tableSchema'"))
                        columnDefinition == null -> validationResult.add(DatabaseSchemaValidationItem("Column definition not found for schema column '$columnSchema' in table '$tableDefinition'"))
                        else -> {

                        }
                    }
                }
            }
        }
        return@use validationResult
    }

    fun createSchema() = connection.createTransaction().use { transaction ->
        val statements = transaction.createSchemaStatements(tables)
        for (statement in statements) {
            transaction.executeStatement(statement)
        }
    }

    fun Transaction.createSchemaStatements(tables: List<Table>): List<String> {
        val statements = ArrayList<String>()
        if (tables.isEmpty())
            return statements

        val existingTables = querySchema().tables().toList()
        for (table in tables) {
            if (existingTables.any { it.name == table.tableName.identifier })
                continue

            // create table
            val tableDefinition = connection.dialect.tableDefinitionSQL(table)
            statements.add(tableDefinition)

            // create indices
/*
            for (table_index in table.indices) {
                statements.add(createIndex(table_index.first, table_index.second))
            }
*/
        }
        return statements
    }

}

