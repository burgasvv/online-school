package org.burgas.service

import kotlinx.serialization.json.Json
import org.burgas.dao.IdentityEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.IdentityRequest
import org.burgas.dto.IdentityResponse
import org.burgas.redis.CacheHandler
import org.burgas.redis.RedisKeys
import org.burgas.service.contract.CollectService
import org.burgas.service.contract.CreateService
import org.burgas.service.contract.ReadService
import org.burgas.service.contract.UpdateService
import org.burgas.service.contract.DeleteService
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.*

class IdentityService : CacheHandler<IdentityResponse>, CollectService<IdentityResponse>, ReadService<UUID, IdentityEntity, IdentityResponse>,
    CreateService<IdentityRequest, IdentityResponse>, UpdateService<IdentityRequest, IdentityResponse>, DeleteService<UUID> {

    private val redis = DatabaseConnection.jedis

    override suspend fun handleCache(response: IdentityResponse) {
        val identityKey = RedisKeys.IDENTITY_KEY.format(response.id)
        if (redis.exists(identityKey)) redis.del(identityKey)
    }

    override suspend fun findAll(): Set<IdentityResponse> = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        IdentityEntity.all().map { it.toResponse() }.toSet()
    }

    override suspend fun findEntity(id: UUID): IdentityEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        IdentityEntity.findById(id)!!
    }

    override suspend fun findById(id: UUID): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        val identityKey = RedisKeys.IDENTITY_KEY.format(id)
        if (redis.exists(identityKey)) {
            Json.decodeFromString<IdentityResponse>(redis.get(identityKey))
        } else {
            val identityResponse = findEntity(id).toResponse()
            redis.set(identityKey, Json.encodeToString(identityResponse))
            identityResponse
        }
    }

    override suspend fun create(request: IdentityRequest): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityResponse = IdentityEntity.new { insert(request) }.toResponse()
        handleCache(identityResponse)
        val identityKey = RedisKeys.IDENTITY_KEY.format(identityResponse.id)
        redis.set(identityKey, Json.encodeToString(identityResponse))
        identityResponse
    }

    override suspend fun update(request: IdentityRequest): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityResponse = IdentityEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!.toResponse()
        handleCache(identityResponse)
        val identityKey = RedisKeys.IDENTITY_KEY.format(identityResponse.id)
        redis.set(identityKey, Json.encodeToString(identityResponse))
        identityResponse
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        val identityEntity = findEntity(id)
        handleCache(identityEntity.toResponse())
        identityEntity.delete()
    }
}