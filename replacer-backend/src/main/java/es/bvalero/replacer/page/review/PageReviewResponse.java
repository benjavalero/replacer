package es.bvalero.replacer.page.review;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Collection;
import lombok.Value;
import org.springframework.lang.NonNull;

@ApiModel(description = "Page and replacements to review")
@Value(staticConstructor = "of")
class PageReviewResponse {

    @ApiModelProperty(required = true)
    @NonNull
    ReviewPage page;

    @ApiModelProperty(value = "Collection of replacements to review", required = true)
    @NonNull
    Collection<ReviewReplacement> replacements;

    @ApiModelProperty(required = true)
    @NonNull
    PageReviewSearch search;

    @ApiModelProperty(
        value = "Number of pending pages to review of the given type",
        required = true,
        example = "1704147"
    )
    @NonNull
    Long numPending;
}