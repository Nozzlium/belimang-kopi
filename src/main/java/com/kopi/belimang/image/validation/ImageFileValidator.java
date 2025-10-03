package com.kopi.belimang.image.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class ImageFileValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    private static final long MIN_SIZE = 10 * 1024;   // 10 KB
    private static final long MAX_SIZE = 2 * 1024 * 1024; // 2 MB

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !(filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg"))) {
            return false;
        }

        long size = file.getSize();
        return size >= MIN_SIZE && size <= MAX_SIZE;
    }
}
