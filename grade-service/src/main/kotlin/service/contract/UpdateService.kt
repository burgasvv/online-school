package org.burgas.service.contract

import org.burgas.dto.Request

interface UpdateService<in R : Request> {

    suspend fun update(request: R)
}