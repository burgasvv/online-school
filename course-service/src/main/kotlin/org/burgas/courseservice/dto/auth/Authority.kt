package org.burgas.courseservice.dto.auth

import org.springframework.security.core.GrantedAuthority

enum class Authority : GrantedAuthority {

    ADMIN, TEACHER, USER;

    override fun getAuthority(): String {
        return this.name
    }
}