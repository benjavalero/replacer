package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

@UtilityClass
public class PageReviewMapper {

    PageReviewResponse toDto(PageReview review, PageReviewOptions options) {
        return PageReviewResponse.of(
            toDto(review.getPage(), review.getSection()),
            toDto(review.getReplacements()),
            toDto(options),
            review.getNumPending()
        );
    }

    private ReviewPage toDto(WikipediaPage page, @Nullable WikipediaSection section) {
        ReviewPage reviewPage = new ReviewPage();
        reviewPage.setLang(page.getId().getLang());
        reviewPage.setId(page.getId().getPageId());
        reviewPage.setTitle(page.getTitle());
        reviewPage.setContent(page.getContent());
        reviewPage.setSection(toDto(section));
        reviewPage.setQueryTimestamp(WikipediaDateUtils.formatWikipediaTimestamp(page.getQueryTimestamp()));
        return reviewPage;
    }

    @Nullable
    private ReviewSection toDto(@Nullable WikipediaSection section) {
        if (section == null) {
            return null;
        } else {
            ReviewSection reviewSection = new ReviewSection();
            reviewSection.setId(section.getIndex());
            reviewSection.setTitle(section.getAnchor());
            return reviewSection;
        }
    }

    private Collection<ReviewReplacement> toDto(Collection<Replacement> replacements) {
        return replacements.stream().map(PageReviewMapper::toDto).collect(Collectors.toUnmodifiableList());
    }

    private ReviewReplacement toDto(Replacement replacement) {
        return ReviewReplacement.of(
            replacement.getStart(),
            replacement.getText(),
            replacement.getSuggestions().stream().map(PageReviewMapper::toDto).collect(Collectors.toList())
        );
    }

    private ReviewSuggestion toDto(Suggestion suggestion) {
        return ReviewSuggestion.of(suggestion.getText(), suggestion.getComment());
    }

    private PageReviewSearch toDto(PageReviewOptions options) {
        PageReviewSearch search = new PageReviewSearch();
        if (options.getOptionsType() != PageReviewOptionsType.NO_TYPE) {
            search.setType(options.getType());
            search.setSubtype(options.getSubtype());
            search.setSuggestion(options.getSuggestion());
            search.setCs(options.getCs());
        }
        return search;
    }

    public PageReviewOptions fromDto(PageReviewSearch search, WikipediaLanguage lang, String user) {
        PageReviewOptions pageReviewOptions = new PageReviewOptions();
        pageReviewOptions.setLang(lang);
        pageReviewOptions.setUser(user);
        pageReviewOptions.setType(search.getType());
        pageReviewOptions.setSubtype(search.getSubtype());
        pageReviewOptions.setSuggestion(search.getSuggestion());
        pageReviewOptions.setCs(search.getCs());
        return pageReviewOptions;
    }
}
