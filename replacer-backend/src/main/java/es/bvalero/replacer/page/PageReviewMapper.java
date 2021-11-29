package es.bvalero.replacer.page;

import es.bvalero.replacer.page.review.PageReviewDto;
import lombok.experimental.UtilityClass;

@UtilityClass
class PageReviewMapper {

    PageReviewDto toDto(PageReview review) {
        return PageReviewDto.of(review.getPage(), review.getReplacements(), review.getSearch());
    }
}
