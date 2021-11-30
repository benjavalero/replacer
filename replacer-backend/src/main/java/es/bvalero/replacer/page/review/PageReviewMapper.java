package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.Suggestion;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaSection;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

@UtilityClass
public class PageReviewMapper {

    // TODO: Public while refactoring

    // TODO: Public while refactoring
    public PageReviewDto toDto(PageReview review) {
        return PageReviewDto.of(
            toDto(review.getPage(), review.getSection()),
            toDto(review.getReplacements()),
            toDto(review.getOptions()),
            review.getNumPending()
        );
    }

    private PageDto toDto(WikipediaPage page, @Nullable WikipediaSection section) {
        return PageDto
            .builder()
            .lang(page.getId().getLang())
            .id(page.getId().getPageId())
            .title(page.getTitle())
            .content(page.getContent())
            .section(toDto(section))
            .queryTimestamp(WikipediaDateUtils.formatWikipediaTimestamp(page.getQueryTimestamp()))
            .build();
    }

    @Nullable
    private PageSection toDto(@Nullable WikipediaSection section) {
        if (section == null) {
            return null;
        } else {
            return PageSection.of(section.getIndex(), section.getAnchor());
        }
    }

    private Collection<PageReplacement> toDto(Collection<Replacement> replacements) {
        return replacements.stream().map(PageReviewMapper::toDto).collect(Collectors.toUnmodifiableList());
    }

    private PageReplacement toDto(Replacement replacement) {
        return PageReplacement.of(
            replacement.getStart(),
            replacement.getText(),
            replacement.getSuggestions().stream().map(PageReviewMapper::toDto).collect(Collectors.toList())
        );
    }

    private PageReplacementSuggestion toDto(Suggestion suggestion) {
        return PageReplacementSuggestion.of(suggestion.getText(), suggestion.getComment());
    }

    private PageReviewSearch toDto(PageReviewOptions options) {
        PageReviewSearch search = new PageReviewSearch();
        if (options.getType() != null) {
            search.setType(options.getType());
            search.setSubtype(options.getSubtype());
            search.setSuggestion(options.getSuggestion());
            search.setCs(options.getCs());
        }
        return search;
    }
}
