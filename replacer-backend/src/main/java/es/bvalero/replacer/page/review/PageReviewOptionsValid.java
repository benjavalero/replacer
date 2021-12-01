package es.bvalero.replacer.page.review;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

@SuppressWarnings("unused")
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { PageReviewOptionsValidator.class })
@interface PageReviewOptionsValid {
    String message() default "Page Review Options not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
