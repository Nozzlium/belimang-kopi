package com.kopi.belimang.image.service;

import com.kopi.belimang.image.dto.UploadImageResponse;
import com.kopi.belimang.image.entity.Image;
import com.kopi.belimang.image.minio.MinioProperties;
import com.kopi.belimang.image.repository.ImageRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ImageService implements IImageService {

    private final MinioClient minioClient;
    private final ImageRepository imageRepository;
    private final MinioProperties properties;

    public ImageService(MinioClient minioClient, ImageRepository imageRepository, MinioProperties properties) {
        this.minioClient = minioClient;
        this.imageRepository = imageRepository;
        this.properties = properties;
    }

    @Async
    @Override
    public CompletableFuture<UploadImageResponse> uploadImage(byte[] imageBytes, String originalFilename) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String uuid = UUID.randomUUID().toString();
                String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
                String fileName = uuid + ext;

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(properties.getBucket())
                                .object(fileName)
                                .stream(new ByteArrayInputStream(imageBytes), imageBytes.length, -1)
                                .contentType("image/jpeg")
                                .build()
                );

                String url = properties.getEndpoint() + "/" + properties.getBucket() + "/" + fileName;

                Image image = new Image();
                image.setUrl(url);
                image.setCreatedAt(Instant.now());
                image.setUpdatedAt(Instant.now());
                imageRepository.save(image);

                return new UploadImageResponse(
                        "File uploaded successfully",
                        new UploadImageResponse.Data(url)
                );
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        });
    }

}
