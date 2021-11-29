package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaSection;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import io.swagger.annotations.ApiModelProperty;
import java.util.Collection;
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
public class PageReview {

    // TODO: Public while refactoring

    @ApiModelProperty(required = true)
    PageDto page;

    @ApiModelProperty(value = "List of replacements to review", required = true)
    List<PageReplacement> replacements;

    @ApiModelProperty(value = "Search options of the replacements to review", required = true)
    PageReviewSearch search;

    // TODO: Public while refactoring
    public static PageReview of(
        WikipediaPage page,
        @Nullable WikipediaSection section,
        Collection<Replacement> replacements,
        PageReviewSearch search
    ) {
        return PageReview
            .builder()
            .page(convert(page, section))
            .replacements(PageReviewService.convert(replacements))
            .search(search)
            .build();
    }

    private static PageDto convert(WikipediaPage page, @Nullable WikipediaSection section) {
        return PageDto
            .builder()
            .lang(page.getId().getLang())
            .id(page.getId().getPageId())
            .title(page.getTitle())
            .content(page.getContent())
            .section(convert(section))
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
