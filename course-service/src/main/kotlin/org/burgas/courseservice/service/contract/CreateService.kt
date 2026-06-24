package org.burgas.courseservice.service.contract

import org.burgas.courseservice.dto.Request
import org.burgas.courseservice.dto.Response

interface CreateService<in Req : Request, out Res : Response> {

    fun create(request: Req): Res
}