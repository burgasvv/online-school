package org.burgas.database

import io.ktor.server.config.*
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

object DatabaseConnection {

    private val config = ApplicationConfig("application.yaml")
    private val options = ConnectionFactoryOptions
        .parse(config.property("ktor.postgres.url").getString()).mutate()
        .option(ConnectionFactoryOptions.USER, config.property("ktor.postgres.user").getString())
        .option(ConnectionFactoryOptions.PASSWORD, config.property("ktor.postgres.password").getString())
        .option(Option.valueOf("initialSize"), config.property("ktor.postgres.pool.initialSize").getString())
        .option(Option.valueOf("maxSize"), config.property("ktor.postgres.pool.maxSize").getString())
        .build()
    private val connectionPoolFactory = ConnectionFactories.get(options)
    val postgres = R2dbcDatabase.connect(
        connectionFactory = connectionPoolFactory,
        databaseConfig = R2dbcDatabaseConfig { explicitDialect = PostgreSQLDialect() }
    )
}

object GradeTable : Table("grade") {
    val projectId = javaUUID("project_id")
    val studentId = javaUUID("student_id")
    val teacherId = javaUUID("teacher_id")
    val description = text("description").nullable()
    val mark = integer("mark").default(0).check { (it greaterEq 0) and (it lessEq 5) }
    val date = datetime("date").defaultExpression(CurrentDateTime)

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(arrayOf(projectId, studentId, teacherId))

    init {
        index(false, mark)
        index(false, date)
    }
}

suspend fun configureDatabase() {
    suspendTransaction(db = DatabaseConnection.postgres) {
        SchemaUtils.create(GradeTable)
    }
}