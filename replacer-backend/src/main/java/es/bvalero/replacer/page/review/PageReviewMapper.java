package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.dto.PageReviewOptionsDto;
import es.bvalero.replacer.common.dto.ReviewPage;
import es.bvalero.replacer.common.util.WikipediaDateUtils;
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
        reviewPage.setLang(page.getId().getLang().getCode());
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

    private Collection<ReviewReplacement> toDto(Collection<PageReplacement> replacements) {
        return replacements.stream().map(PageReviewMapper::toDto).collect(Collectors.toUnmodifiableList());
    }

    private ReviewReplacement toDto(PageReplacement replacement) {
        return ReviewReplacement.of(
            replacement.getStart(),
            replacement.getText(),
            replacement.getSuggestions().stream().map(PageReviewMapper::toDto).collect(Collectors.toList())
        );
    }

    private ReviewSuggestion toDto(Suggestion suggestion) {
        return ReviewSuggestion.of(suggestion.getText(), suggestion.getComment());
    }

    private PageReviewOptionsDto toDto(PageReviewOptions options) {
        PageReviewOptionsDto dto = new PageReviewOptionsDto();
        if (options.getOptionsType() != PageReviewOptionsType.NO_TYPE) {
            dto.setType(options.getType().getKind().getCode());
            dto.setSubtype(options.getType().getSubtype());
            dto.setSuggestion(options.getSuggestion());
            dto.setCs(options.getCs());
        }
        return dto;
    }

    public PageReviewOptions fromDto(PageReviewOptionsDto options, CommonQueryParameters queryParameters) {
        return PageReviewOptions
            .builder()
            .lang(WikipediaLanguage.valueOfCode(queryParameters.getLang()))
            .user(queryParameters.getUser())
            .type(ReplacementType.of(options.getType(), options.getSubtype()))
            .suggestion(options.getSuggestion())
            .cs(options.getCs())
            .build();
    }
}
