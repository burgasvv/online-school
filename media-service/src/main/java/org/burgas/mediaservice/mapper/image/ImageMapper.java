package org.burgas.mediaservice.mapper.image;

import lombok.SneakyThrows;
import org.burgas.mediaservice.dao.image.Image;
import org.burgas.mediaservice.dto.image.ImageResponse;
import org.burgas.mediaservice.mapper.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Component
public class ImageMapper implements Mapper<Image, ImageResponse> {

    @SneakyThrows
    @Override
    public Image toDao(MultipartFile multipartFile) {
        if (Objects.requireNonNull(multipartFile.getContentType()).startsWith("image")) {
            return Image.builder()
                    .name(multipartFile.getOriginalFilename())
                    .contentType(multipartFile.getContentType())
                    .preview(true)
                    .size(multipartFile.getSize())
                    .data(multipartFile.getBytes())
                    .build();
        } else {
            throw new IllegalArgumentException("Part file is not image content type");
        }
    }

    @Override
    public ImageResponse toResponse(Image dao) {
        return ImageResponse.builder()
                .id(dao.getId())
                .name(dao.getName())
                .contentType(dao.getContentType())
                .preview(dao.getPreview())
                .size(dao.getSize())
                .build();
    }
}
