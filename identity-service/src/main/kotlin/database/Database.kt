
package org.burgas.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

object DatabaseConnection {

    private val config = ApplicationConfig("application.yaml")

    private val hikariConfig = HikariConfig().apply {
        driverClassName = config.property("ktor.postgres.driver").getString()
        jdbcUrl = config.property("ktor.postgres.url").getString()
        username = config.property("ktor.postgres.username").getString()
        password = config.property("ktor.postgres.password").getString()
        maximumPoolSize = 50
        minimumIdle = 5
        validate()
    }

    private val dataSource = HikariDataSource(hikariConfig)

    val postgres = Database.connect(datasource = dataSource)
}

enum class Authority {
    STUDENT, TEACHER, ADMIN
}

object IdentityTable : UUIDTable("identity") {
    val authority = enumerationByName<Authority>("authority", 250)
    val email = varchar("email", 250).uniqueIndex()
    val password = varchar("password", 250)
    val status = bool("status").default(true)
    val firstname = varchar("firstname", 250)
    val lastname = varchar("lastname", 250)
    val patronymic = varchar("patronymic", 250)
    val about = text("about").nullable()
    val imageId = javaUUID("image_id").uniqueIndex().nullable()
    init {
        index(false, firstname, lastname, patronymic)
    }
}

object IdentityDocumentTable : Table("identity_document") {
    val identityId = reference(
        name = "identity_id", refColumn = IdentityTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val documentId = javaUUID("document_id")
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(arrayOf(identityId, documentId))
}

suspend fun configureDatabase() {
    suspendTransaction(db = DatabaseConnection.postgres) {
        SchemaUtils.create(IdentityTable, IdentityDocumentTable)
    }
}