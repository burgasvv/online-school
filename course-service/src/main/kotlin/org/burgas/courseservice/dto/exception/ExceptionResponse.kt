package org.burgas.courseservice.dto.exception

import org.burgas.courseservice.dto.Response

data class ExceptionResponse(
    val status: String,
    val code: Int,
    val message: String?
) : Response
