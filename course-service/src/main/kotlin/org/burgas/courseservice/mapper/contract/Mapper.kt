package org.burgas.courseservice.mapper.contract

import org.burgas.courseservice.dao.Dao
import org.burgas.courseservice.dto.Dependency
import org.burgas.courseservice.dto.Request
import org.burgas.courseservice.dto.Response

interface Mapper<Req : Request, Ent : Dao, Dep : Dependency, Res : Response> {

    fun toEntity(request: Req): Ent

    fun toDependency(entity: Ent): Dep

    fun toResponse(entity: Ent): Res
}