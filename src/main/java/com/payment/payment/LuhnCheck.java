package com.payment.payment;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.payment.payment.validation.LuhnValidator;

@Documented
@Constraint(validatedBy = LuhnValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LuhnCheck {
    String message() default "Invalid credit card number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}