package org.burgas.service.contract

import org.burgas.dto.Request

interface CreateService<in R : Request> {

    suspend fun create(request: R)
}