package com.kopi.belimang.image.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ImageFileValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImage {
    String message() default "Invalid image (must be .jpg/.jpeg, size between 10KB and 2MB)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
