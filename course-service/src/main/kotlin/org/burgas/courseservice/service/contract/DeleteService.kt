package org.burgas.courseservice.service.contract

interface DeleteService<in ID> {

    fun delete(id: ID)
}