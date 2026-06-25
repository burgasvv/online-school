package org.burgas.courseservice.security

import jakarta.servlet.http.HttpServletRequest
import org.burgas.courseservice.dto.auth.AuthToken
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        httpSecurity {
            cors { disable() }
            csrf { disable() }
            exceptionHandling { authenticationEntryPoint = HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED) }
            authorizeHttpRequests {
                authorize("/api/v1/courses/by-identity", permitAll)
                authorize("/api/v1/courses", authenticated)
                authorize("/api/v1/courses/by-id", authenticated)
                authorize("/api/v1/courses/create", hasAnyAuthority("ADMIN", "TEACHER"))
                authorize("/api/v1/courses/update", hasAnyAuthority("ADMIN", "TEACHER"))
                authorize("/api/v1/courses/delete", hasAnyAuthority("ADMIN", "TEACHER"))
                authorize("/api/v1/courses/add-identity", authenticated)
                authorize("/api/v1/courses/remove-identity", authenticated)
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter> { request, response, chain ->
                request as HttpServletRequest
                val cookie = request.cookies?.find { it.name == "AUTH_TOKEN" }
                if (cookie != null) {
                    try {
                        val jsonString = URLDecoder.decode(cookie.value, StandardCharsets.UTF_8)
                        val mapper = ObjectMapper()
                        val authToken = mapper.readValue<AuthToken>(jsonString)
                        val authenticationToken = UsernamePasswordAuthenticationToken(
                            authToken.token, null, listOf(authToken.authority)
                        )
                        SecurityContextHolder.getContext().authentication = authenticationToken
                    } catch (e: Exception) {
                        SecurityContextHolder.clearContext()
                        throw e
                    }
                }
                chain.doFilter(request, response)
            }
        }
        return httpSecurity.build()
    }
}