package org.burgas.mediaservice.service;

import lombok.RequiredArgsConstructor;
import org.burgas.mediaservice.dao.image.Image;
import org.burgas.mediaservice.dto.image.ImageResponse;
import org.burgas.mediaservice.mapper.image.ImageMapper;
import org.burgas.mediaservice.repository.ImageRepository;
import org.burgas.mediaservice.service.contract.FindService;
import org.burgas.mediaservice.service.contract.RemoveService;
import org.burgas.mediaservice.service.contract.UploadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class ImageService implements FindService<UUID, Image, ImageResponse>, UploadService<ImageResponse>, RemoveService<UUID> {

    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;

    @Override
    public Image findEntity(UUID uuid) {
        return imageRepository.findById(uuid).orElse(null);
    }

    @Override
    public ImageResponse findById(UUID uuid) {
        Image image = findEntity(uuid);
        return image != null ? imageMapper.toResponse(image) : null;
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Throwable.class, Exception.class}
    )
    public ImageResponse upload(MultipartFile multipartFile) {
        Image image = imageRepository.save(imageMapper.toDao(multipartFile));
        return imageMapper.toResponse(image);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Throwable.class, Exception.class}
    )
    public void remove(UUID uuid) {
        Image image = findEntity(uuid);
        imageRepository.delete(image);
    }
}
