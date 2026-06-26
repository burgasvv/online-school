package org.burgas.service.contract

import org.burgas.dto.Response
import org.jetbrains.exposed.v1.core.ResultRow

interface ReadService<ID, R : Response> {

    suspend fun findRows(id: ID): ResultRow

    suspend fun findById(id: ID): R
}