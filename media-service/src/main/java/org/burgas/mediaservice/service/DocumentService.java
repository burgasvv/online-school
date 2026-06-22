package org.burgas.mediaservice.service;

import lombok.RequiredArgsConstructor;
import org.burgas.mediaservice.dao.document.Document;
import org.burgas.mediaservice.dto.document.DocumentResponse;
import org.burgas.mediaservice.mapper.document.DocumentMapper;
import org.burgas.mediaservice.repository.DocumentRepository;
import org.burgas.mediaservice.service.contract.FindService;
import org.burgas.mediaservice.service.contract.RemoveService;
import org.burgas.mediaservice.service.contract.UploadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class DocumentService implements FindService<UUID, Document, DocumentResponse>, UploadService<DocumentResponse>, RemoveService<UUID> {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    @Override
    public Document findEntity(UUID uuid) {
        return documentRepository.findById(uuid).orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }

    @Override
    public DocumentResponse findById(UUID uuid) {
        Document document = findEntity(uuid);
        return documentMapper.toResponse(document);
    }

    public Set<DocumentResponse> findByIds(final Set<UUID> documentIds) {
        return documentRepository.findAllById(documentIds)
                .parallelStream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Throwable.class, Exception.class}
    )
    public DocumentResponse upload(MultipartFile multipartFile) {
        Document document = documentRepository.save(documentMapper.toDao(multipartFile));
        return documentMapper.toResponse(document);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Throwable.class, Exception.class}
    )
    public void remove(UUID uuid) {
        Document document = findEntity(uuid);
        documentRepository.delete(document);
    }
}
