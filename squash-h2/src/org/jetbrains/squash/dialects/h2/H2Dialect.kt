package org.jetbrains.squash.dialects.h2

import org.h2.jdbcx.*
import org.jetbrains.squash.*
import org.jetbrains.squash.definition.*
import org.jetbrains.squash.dialect.*
import org.jetbrains.squash.drivers.*

object H2Dialect : BaseSQLDialect("H2") {
    override val definition: DefinitionSQLDialect = object : BaseDefinitionSQLDialect(this) {
        override fun columnTypeSQL(builder: SQLBuilder, type: ColumnType) : Unit {
            when (type) {
                is UUIDColumnType -> builder.append("UUID")
                else -> super.columnTypeSQL(builder, type)
            }
        }
    }

    fun createMemoryConnection(catalogue: String = "", user: String = "", password: String = ""): DatabaseConnection {
        val pool = JdbcConnectionPool.create("jdbc:h2:mem:$catalogue", user, password);
        return JDBCConnection(H2Dialect) { pool.connection }
    }
}
