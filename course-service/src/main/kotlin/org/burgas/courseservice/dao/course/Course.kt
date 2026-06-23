package org.burgas.courseservice.dao.course

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.burgas.courseservice.dao.Dao
import org.burgas.courseservice.dao.project.Project
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "course", schema = "public")
class Course : Dao {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    lateinit var id: UUID

    @Column(name = "name", columnDefinition = "varchar")
    lateinit var name: String

    @Column(name = "description", columnDefinition = "text")
    lateinit var description: String

    @Column(name = "date", columnDefinition = "timestamp")
    lateinit var date: LocalDateTime

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var projects: MutableSet<Project> = mutableSetOf()
}