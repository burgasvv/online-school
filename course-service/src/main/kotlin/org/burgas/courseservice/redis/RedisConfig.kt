package org.burgas.courseservice.redis

import org.burgas.courseservice.dto.course.CourseResponse
import org.burgas.courseservice.dto.project.ProjectResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun courseRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, CourseResponse> {
        val template = RedisTemplate<String, CourseResponse>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.valueSerializer = JacksonJsonRedisSerializer(CourseResponse::class.java)
        template.hashValueSerializer = JacksonJsonRedisSerializer(CourseResponse::class.java)
        template.afterPropertiesSet()
        return template
    }

    @Bean
    fun projectRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, ProjectResponse> {
        val template = RedisTemplate<String, ProjectResponse>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.valueSerializer = JacksonJsonRedisSerializer(ProjectResponse::class.java)
        template.hashValueSerializer = JacksonJsonRedisSerializer(ProjectResponse::class.java)
        template.afterPropertiesSet()
        return template
    }
}