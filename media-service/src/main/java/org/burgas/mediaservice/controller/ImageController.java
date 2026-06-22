package org.burgas.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.burgas.mediaservice.dao.image.Image;
import org.burgas.mediaservice.dto.image.ImageResponse;
import org.burgas.mediaservice.service.ImageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class  ImageController {

    private final ImageService imageService;

    @GetMapping("/data/by-id")
    public ResponseEntity<Resource> getImageResource(@RequestParam UUID imageId) {
        Image image = imageService.findEntity(imageId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(new InputStreamResource(new ByteArrayInputStream(image.getData())));
    }

    @GetMapping("/by-id")
    public ResponseEntity<ImageResponse> getImageResponse(@RequestParam UUID imageId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(imageService.findById(imageId));
    }

    @PostMapping("/upload")
    public ResponseEntity<ImageResponse> uploadImage(@RequestPart MultipartFile image) {
        ImageResponse imageResponse = imageService.upload(image);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(imageResponse);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeImage(@RequestParam UUID imageId) {
        imageService.remove(imageId);
        return ResponseEntity.noContent().build();
    }
}
