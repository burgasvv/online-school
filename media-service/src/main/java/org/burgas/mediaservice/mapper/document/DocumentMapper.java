package org.burgas.mediaservice.mapper.document;

import lombok.SneakyThrows;
import org.burgas.mediaservice.dao.document.Document;
import org.burgas.mediaservice.dto.document.DocumentResponse;
import org.burgas.mediaservice.mapper.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class DocumentMapper implements Mapper<Document, DocumentResponse> {

    @SneakyThrows
    @Override
    public Document toDao(MultipartFile multipartFile) {
        return Document.builder()
                .name(multipartFile.getOriginalFilename())
                .contentType(multipartFile.getContentType())
                .size(multipartFile.getSize())
                .data(multipartFile.getBytes())
                .build();
    }

    @Override
    public DocumentResponse toResponse(Document dao) {
        return DocumentResponse.builder()
                .id(dao.getId())
                .name(dao.getName())
                .contentType(dao.getContentType())
                .size(dao.getSize())
                .build();
    }
}
