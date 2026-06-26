package org.burgas.database

import io.ktor.server.config.*
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.burgas.client.RestClient
import org.burgas.dto.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

interface Creator<in R : Request> {
    fun InsertStatement<Number>.insert(request: R)
}

interface Updater<in R : Request> {
    fun UpdateStatement.update(request: R)
}

interface DependencyMapper<out D : Dependency> {
    suspend fun ResultRow.toDependency(): D
}

interface ResponseMapper<out R : Response> {
    suspend fun ResultRow.toResponse(): R
}

object GradeTable : UUIDTable("grade"),
    Creator<GradeRequest>, Updater<GradeRequest>,
    DependencyMapper<GradeDependency>, ResponseMapper<GradeResponse> {

    val projectId = javaUUID("project_id")
    val studentId = javaUUID("student_id")
    val teacherId = javaUUID("teacher_id")
    val description = text("description").nullable()
    val mark = integer("mark").default(0).check { (it greaterEq 0) and (it lessEq 5) }
    val date = datetime("date").defaultExpression(CurrentDateTime)

    init {
        index(false, mark)
        index(false, date)
    }

    override fun InsertStatement<Number>.insert(request: GradeRequest) {
        request.projectId!!.let { this[projectId] = it }
        request.studentId!!.let { this[studentId] = it }
        request.teacherId!!.let { this[teacherId] = it }
        request.description.let { this[description] = it }
        request.mark!!.let { this[mark] = it }
        this[date] = LocalDateTime.now().toKotlinLocalDateTime()
    }

    override fun UpdateStatement.update(request: GradeRequest) {
        request.projectId?.let { this[projectId] = it }
        request.teacherId?.let { this[teacherId] = it }
        request.studentId?.let { this[studentId] = it }
        request.description.let { this[description] = it }
        request.mark?.let { this[mark] = it }
    }

    override suspend fun ResultRow.toDependency(): GradeDependency {
        return GradeDependency(
            id = this[id].value,
            teacher = RestClient.findTeacherDependency(this[teacherId]),
            student = RestClient.findStudentDependency(this[studentId]),
            description = this[description],
            mark = this[mark],
            date = this[date].toJavaLocalDateTime().format(DateTimeFormatter.ofPattern("hh MMMM yyyy, hh:mm"))
        )
    }

    override suspend fun ResultRow.toResponse(): GradeResponse {
        return GradeResponse(
            id = this[id].value,
            project = RestClient.findProjectDependency(this[projectId]),
            teacher = RestClient.findTeacherDependency(this[teacherId]),
            student = RestClient.findStudentDependency(this[studentId]),
            description = this[description],
            mark = this[mark],
            date = this[date].toJavaLocalDateTime().format(DateTimeFormatter.ofPattern("hh MMMM yyyy, hh:mm"))
        )
    }
}

suspend fun configureDatabase() {
    suspendTransaction(db = DatabaseConnection.postgres) {
        SchemaUtils.create(GradeTable)
    }
}