package org.burgas.service.contract

import org.burgas.dao.Dao
import org.burgas.dto.Response

interface ReadService<ID, D : Dao, R : Response> {

    suspend fun findEntity(id: ID): D

    suspend fun findById(id: ID): R
}