package com.kopi.belimang.image.controller;

import com.kopi.belimang.image.dto.UploadImageResponse;
import com.kopi.belimang.image.service.IImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/image")
public class ImageController {

    private final IImageService imageService;

    public ImageController(IImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<UploadImageResponse>> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        byte[] imageBytes = file.getBytes();
        String originalFilename = file.getOriginalFilename();

        return imageService.uploadImage(imageBytes, originalFilename)
                .thenApply(ResponseEntity::ok) // sukses
                .exceptionally(ex -> {
                    // gagal → tetap return UploadImageResponse dengan null data
                    UploadImageResponse errorResponse =
                            new UploadImageResponse("Upload failed: " + ex.getCause().getMessage(), null);
                    return ResponseEntity.status(500).body(errorResponse);
                });
    }
}
