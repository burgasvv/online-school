package org.burgas.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.burgas.mediaservice.dao.document.Document;
import org.burgas.mediaservice.dto.document.DocumentRequest;
import org.burgas.mediaservice.dto.document.DocumentResponse;
import org.burgas.mediaservice.service.DocumentService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/data/by-id")
    public ResponseEntity<Resource> getDocumentDataById(@RequestParam UUID documentId) {
        Document document = documentService.findEntity(documentId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .body(new InputStreamResource(new ByteArrayInputStream(document.getData())));
    }

    @GetMapping("/by-id")
    public ResponseEntity<DocumentResponse> getDocumentById(@RequestParam UUID documentId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentService.findById(documentId));
    }

    @GetMapping("/by-ids")
    public ResponseEntity<Set<DocumentResponse>> getDocumentsByIds(@RequestBody DocumentRequest documentRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentService.findByIds(documentRequest.getDocumentIds()));
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestPart MultipartFile document) {
        DocumentResponse documentResponse = documentService.upload(document);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentResponse);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> removeDocument(@RequestParam UUID documentId) {
        documentService.remove(documentId);
        return ResponseEntity.noContent().build();
    }
}
