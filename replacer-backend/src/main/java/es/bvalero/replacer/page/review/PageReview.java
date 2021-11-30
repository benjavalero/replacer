package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaSection;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Sub-domain object representing a page to review in the front-end */
@NonFinal
@Value
@Builder // TODO: Check
class PageReview {

    @NonNull
    WikipediaPage page;

    @Nullable
    WikipediaSection section;

    @ApiModelProperty(value = "List of replacements to review", required = true)
    @NonNull
    List<Replacement> replacements;

    @ApiModelProperty(value = "Search options of the replacements to review", required = true)
    // TODO: Make NonNull or fill it in the controller
    PageReviewSearch search;

    static PageReview of(
        WikipediaPage page,
        @Nullable WikipediaSection section,
        Collection<Replacement> replacements,
        PageReviewSearch search
    ) {
        return PageReview
            .builder()
            .page(page)
            .section(section)
            .replacements(new ArrayList<>(replacements))
            .search(search)
            .build();
    }
}
