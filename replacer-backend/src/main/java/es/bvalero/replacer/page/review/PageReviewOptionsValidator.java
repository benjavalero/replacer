package es.bvalero.replacer.page.review;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PageReviewOptionsValidator implements ConstraintValidator<PageReviewOptionsValid, PageReviewOptions> {

    public boolean isValid(PageReviewOptions options, ConstraintValidatorContext context) {
        return options.isValid();
    }
}
