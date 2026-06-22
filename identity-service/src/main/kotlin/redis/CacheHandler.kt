package org.burgas.redis

import org.burgas.dto.Response

interface CacheHandler<R : Response> {

    suspend fun handleCache(response: R)
}