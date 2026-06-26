package org.burgas.service

import io.r2dbc.spi.IsolationLevel
import kotlinx.coroutines.flow.single
import org.burgas.database.DatabaseConnection
import org.burgas.database.GradeTable
import org.burgas.database.GradeTable.toResponse
import org.burgas.dto.GradeRequest
import org.burgas.dto.GradeResponse
import org.burgas.service.contract.CreateService
import org.burgas.service.contract.DeleteService
import org.burgas.service.contract.ReadService
import org.burgas.service.contract.UpdateService
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.*

class GradeService : ReadService<UUID, GradeResponse>, CreateService<GradeRequest>, UpdateService<GradeRequest>, DeleteService<UUID> {

    override suspend fun findRows(id: UUID): ResultRow = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        GradeTable.selectAll().where { GradeTable.id eq id }.single()
    }

    override suspend fun findById(id: UUID): GradeResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findRows(id).toResponse()
    }

    override suspend fun create(request: GradeRequest): Unit = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = IsolationLevel.READ_COMMITTED
    ) {
        GradeTable.insert { it.insert(request) }
    }

    override suspend fun update(request: GradeRequest): Unit = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = IsolationLevel.READ_COMMITTED
    ) {
        GradeTable.update({ GradeTable.id eq request.id!! }) { it.update(request) }
    }

    override suspend fun delete(id: UUID): Unit = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = IsolationLevel.READ_COMMITTED
    ) {
        GradeTable.deleteWhere { GradeTable.id eq id }
    }
}