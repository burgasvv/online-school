package org.burgas.courseservice.dto.auth

import org.springframework.security.core.GrantedAuthority

enum class Authority : GrantedAuthority {

    STUDENT, TEACHER, ADMIN;

    override fun getAuthority(): String {
        return this.name
    }
}