package org.burgas.courseservice.dao.project

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.burgas.courseservice.dao.Dao
import org.burgas.courseservice.dao.course.Course
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "project", schema = "public")
class Project : Dao {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    lateinit var id: UUID

    @Column(name = "name", columnDefinition = "varchar")
    lateinit var name: String

    @Column(name = "description", columnDefinition = "text")
    lateinit var description: String

    @Column(name = "link", columnDefinition = "text")
    lateinit var link: String

    @Column(name = "task_id", columnDefinition = "uuid")
    lateinit var taskId: UUID

    @Column(name = "date", columnDefinition = "timestamp")
    lateinit var date: LocalDateTime

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    var course: Course? = null
}