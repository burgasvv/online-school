package org.burgas.mediaservice.mapper;

import org.burgas.mediaservice.dao.Dao;
import org.burgas.mediaservice.dto.Response;
import org.springframework.web.multipart.MultipartFile;

public interface Mapper<D extends Dao, R extends Response> {

    D toDao(final MultipartFile multipartFile);

    R toResponse(D dao);
}
