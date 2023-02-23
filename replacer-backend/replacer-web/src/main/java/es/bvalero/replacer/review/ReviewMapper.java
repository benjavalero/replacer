package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaSection;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

@UtilityClass
class ReviewMapper {

    FindReviewResponse toDto(Review review) {
        return FindReviewResponse.of(
            toDto(review.getPage(), review.getSection()),
            toDto(review.getReplacements()),
            review.getNumPending()
        );
    }

    private ReviewPage toDto(WikipediaPage page, @Nullable WikipediaSection section) {
        ReviewPage reviewPage = new ReviewPage();
        reviewPage.setLang(page.getPageKey().getLang().getCode());
        reviewPage.setPageId(page.getPageKey().getPageId());
        reviewPage.setTitle(page.getTitle());
        reviewPage.setContent(page.getContent());
        reviewPage.setSection(toDto(section));
        reviewPage.setQueryTimestamp(page.getQueryTimestamp().toString());
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
            reviewSection.setOffset(section.getByteOffset());
            return reviewSection;
        }
    }

    private Collection<ReviewReplacement> toDto(Collection<Replacement> replacements) {
        return replacements.stream().map(ReviewMapper::toDto).collect(Collectors.toUnmodifiableList());
    }

    private ReviewReplacement toDto(Replacement replacement) {
        return ReviewReplacement.of(
            replacement.getStart(),
            replacement.getText(),
            replacement.getType().getKind().getCode(),
            replacement.getType().getSubtype(),
            replacement.getSuggestions().stream().map(ReviewMapper::toDto).collect(Collectors.toList())
        );
    }

    private ReviewSuggestion toDto(Suggestion suggestion) {
        return ReviewSuggestion.of(suggestion.getText(), suggestion.getComment());
    }

    ReviewOptions fromDto(ReviewOptionsDto options, CommonQueryParameters queryParameters) {
        return ReviewOptions.of(
            queryParameters.getUserId(),
            options.getKind(),
            options.getSubtype(),
            options.getSuggestion(),
            options.getCs()
        );
    }

    Collection<ReviewedReplacement> fromDto(
        int pageId,
        Collection<ReviewedReplacementDto> reviewed,
        int offset,
        CommonQueryParameters queryParameters
    ) {
        return reviewed
            .stream()
            .map(r -> fromDto(pageId, r, offset, queryParameters))
            .collect(Collectors.toUnmodifiableList());
    }

    private ReviewedReplacement fromDto(
        int pageId,
        ReviewedReplacementDto reviewed,
        int offset,
        CommonQueryParameters queryParameters
    ) {
        ReplacementKind replacementKind = ReplacementKind.valueOf(reviewed.getKind());
        ReplacementType replacementType = replacementKind == ReplacementKind.CUSTOM
            ? ReplacementType.ofCustom(reviewed.getSubtype())
            : ReplacementType.ofType(replacementKind, reviewed.getSubtype());
        return ReviewedReplacement
            .builder()
            .pageKey(PageKey.of(queryParameters.getWikipediaLanguage(), pageId))
            .type(replacementType)
            .cs(reviewed.getCs())
            .start(offset + reviewed.getStart())
            .reviewer(queryParameters.getUserId().getUsername())
            .fixed(reviewed.isFixed())
            .build();
    }
}
