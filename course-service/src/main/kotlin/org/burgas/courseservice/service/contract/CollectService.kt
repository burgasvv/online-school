package org.burgas.courseservice.service.contract

import org.burgas.courseservice.dto.Response

interface CollectService<out R : Response> {

    fun findAll(): Set<R>
}