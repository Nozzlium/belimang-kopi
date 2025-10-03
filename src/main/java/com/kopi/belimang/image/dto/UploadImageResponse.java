package com.kopi.belimang.image.dto;

public record UploadImageResponse(String message, Data data) {
    public record Data(String imageUrl) {}
}
