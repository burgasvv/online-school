package org.burgas.courseservice.dto.auth

data class AuthToken(
    val token: String,
    val authority: Authority
)
