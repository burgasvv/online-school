package org.burgas.courseservice.service.contract

import org.burgas.courseservice.dao.Dao
import org.burgas.courseservice.dto.Response

interface ReadService<in ID, out D : Dao, out R : Response> {

    fun findEntity(id: ID): D

    fun findById(id: ID): R
}