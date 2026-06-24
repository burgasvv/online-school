package org.burgas.courseservice.redis

import org.burgas.courseservice.dto.Response

interface CacheHandler<in R : Response> {

    fun handleCache(response: R)
}