package org.burgas.courseservice.service.contract

import org.burgas.courseservice.dto.Request
import org.burgas.courseservice.dto.Response

interface UpdateService<in Req : Request, out Res : Response> {

    fun update(request: Req): Res
}