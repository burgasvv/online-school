package org.burgas.mediaservice.service.contract;

import org.burgas.mediaservice.dao.Dao;
import org.burgas.mediaservice.dto.Response;

import java.util.UUID;

public interface FindService<ID extends UUID, D extends Dao, R extends Response> {

    D findEntity(ID id);

    R findById(ID id);
}
