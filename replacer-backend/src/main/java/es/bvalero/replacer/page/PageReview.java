package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.WikipediaPageSection;
import es.bvalero.replacer.common.domain.WikipediaSection;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.Nullable;

/**
 * Domain class of a page to review to be used in the front-end.
 */
@Value
@Builder
class PageReview {

    @ApiModelProperty(required = true)
    PageDto page;

    @ApiModelProperty(value = "List of replacements to review", required = true)
    List<PageReplacement> replacements;

    @ApiModelProperty(value = "Search options of the replacements to review", required = true)
    PageReviewSearch search;

    static PageReview of(WikipediaPageSection page, List<PageReplacement> replacements, PageReviewSearch search) {
        return PageReview.builder().page(convert(page)).replacements(replacements).search(search).build();
    }

    private static PageDto convert(WikipediaPageSection page) {
        return PageDto
            .builder()
            .lang(page.getId().getLang())
            .id(page.getId().getPageId())
            .title(page.getTitle())
            .content(page.getContent())
            .section(convert(page.getSection()))
            .queryTimestamp(WikipediaDateUtils.formatWikipediaTimestamp(page.getQueryTimestamp()))
            .build();
    }

    @Nullable
    private static PageSection convert(@Nullable WikipediaSection section) {
        if (section == null) {
            return null;
        } else {
            return PageSection.of(section.getIndex(), section.getAnchor());
        }
    }

    @TestOnly
    static PageReview ofEmpty() {
        return PageReview.builder().page(new PageDto()).build();
    }
}
