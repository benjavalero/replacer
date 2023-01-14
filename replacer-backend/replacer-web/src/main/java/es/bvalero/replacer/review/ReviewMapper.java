package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.dto.Language;
import es.bvalero.replacer.common.util.WikipediaDateUtils;
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

    FindReviewResponse toDto(Review review, ReviewOptions options) {
        return FindReviewResponse.of(
            toDto(review.getPage(), review.getSection()),
            toDto(review.getReplacements(), options),
            toDto(options),
            review.getNumPending()
        );
    }

    private ReviewPageDto toDto(WikipediaPage page, @Nullable WikipediaSection section) {
        ReviewPageDto reviewPage = new ReviewPageDto();
        reviewPage.setLang(Language.valueOf(page.getPageKey().getLang().getCode()));
        reviewPage.setId(page.getPageKey().getPageId());
        reviewPage.setTitle(page.getTitle());
        reviewPage.setContent(page.getContent());
        reviewPage.setSection(toDto(section));
        reviewPage.setQueryTimestamp(WikipediaDateUtils.formatWikipediaTimestamp(page.getQueryTimestamp()));
        return reviewPage;
    }

    @Nullable
    private ReviewSectionDto toDto(@Nullable WikipediaSection section) {
        if (section == null) {
            return null;
        } else {
            ReviewSectionDto reviewSection = new ReviewSectionDto();
            reviewSection.setId(section.getIndex());
            reviewSection.setTitle(section.getAnchor());
            reviewSection.setOffset(section.getByteOffset());
            return reviewSection;
        }
    }

    private Collection<ReviewReplacementDto> toDto(Collection<Replacement> replacements, ReviewOptions options) {
        return replacements.stream().map(r -> toDto(r, options)).collect(Collectors.toUnmodifiableList());
    }

    private ReviewReplacementDto toDto(Replacement replacement, ReviewOptions options) {
        return ReviewReplacementDto.of(
            replacement.getStart(),
            replacement.getText(),
            replacement.getType().getKind().getCode(),
            replacement.getType().getSubtype(),
            replacement.getType().getKind() == ReplacementKind.CUSTOM ? options.getCs() : null,
            replacement.getSuggestions().stream().map(ReviewMapper::toDto).collect(Collectors.toList())
        );
    }

    private ReviewSuggestionDto toDto(Suggestion suggestion) {
        return ReviewSuggestionDto.of(suggestion.getText(), suggestion.getComment());
    }

    private ReviewOptionsDto toDto(ReviewOptions options) {
        ReviewOptionsDto dto = new ReviewOptionsDto();
        if (options.getOptionsType() != ReviewOptionsType.NO_TYPE) {
            dto.setKind(options.getType().getKind().getCode());
            dto.setSubtype(options.getType().getSubtype());
            dto.setSuggestion(options.getSuggestion());
            dto.setCs(options.getCs());
        }
        return dto;
    }

    ReviewOptions fromDto(ReviewOptionsDto options, CommonQueryParameters queryParameters) {
        return ReviewOptions
            .builder()
            .lang(queryParameters.getLang())
            .user(queryParameters.getUser())
            .type(ReplacementType.of(options.getKind(), options.getSubtype()))
            .suggestion(options.getSuggestion())
            .cs(options.getCs())
            .build();
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
        return ReviewedReplacement
            .builder()
            .pageKey(PageKey.of(queryParameters.getLang(), pageId))
            .type(ReplacementType.of(reviewed.getKind(), reviewed.getSubtype()))
            .cs(reviewed.getCs())
            .start(offset + reviewed.getStart())
            .reviewer(queryParameters.getUser())
            .fixed(reviewed.isFixed())
            .build();
    }
}