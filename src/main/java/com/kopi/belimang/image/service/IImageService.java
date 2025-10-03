package com.kopi.belimang.image.service;

import com.kopi.belimang.image.dto.UploadImageResponse;

import java.util.concurrent.CompletableFuture;

public interface IImageService {
    CompletableFuture<UploadImageResponse> uploadImage(byte[] imageBytes, String originalFilename);
}
