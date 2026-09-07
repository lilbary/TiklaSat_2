package com.gib.tiklasat.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * BR-U-006 — parola gücü kuralı.
 *
 * En az 10 karakter, büyük harf + küçük harf + rakam, ve yaygın parola
 * kara listesinde bulunmama. Üçü tek anotasyonda toplandı ki kural
 * değişirse tek yerden güncellensin.
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    String message() default "Parola güvenlik kurallarını karşılamıyor.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
