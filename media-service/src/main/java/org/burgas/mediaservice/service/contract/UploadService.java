package org.burgas.mediaservice.service.contract;

import org.burgas.mediaservice.dto.Response;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService<R extends Response> {

    R upload(final MultipartFile multipartFile);
}
