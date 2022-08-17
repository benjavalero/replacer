package es.bvalero.replacer.review.dto;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.util.WikipediaDateUtils;
import es.bvalero.replacer.review.save.ReviewedReplacement;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

@UtilityClass
public class ReviewMapper {

    public FindReviewResponse toDto(Review review, ReviewOptions options) {
        return FindReviewResponse.of(
            toDto(review.getPage(), review.getSection()),
            toDto(review.getReplacements(), options),
            toDto(options),
            review.getNumPending()
        );
    }

    private ReviewPageDto toDto(WikipediaPage page, @Nullable WikipediaSection section) {
        ReviewPageDto reviewPage = new ReviewPageDto();
        reviewPage.setLang(page.getId().getLang().getCode());
        reviewPage.setId(page.getId().getPageId());
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

    public ReviewOptions fromDto(ReviewOptionsDto options, CommonQueryParameters queryParameters) {
        return ReviewOptions
            .builder()
            .lang(WikipediaLanguage.valueOfCode(queryParameters.getLang()))
            .type(ReplacementType.of(options.getKind(), options.getSubtype()))
            .suggestion(options.getSuggestion())
            .cs(options.getCs())
            .build();
    }

    public Collection<ReviewedReplacement> fromDto(
        int pageId,
        Collection<ReviewedReplacementDto> reviewed,
        CommonQueryParameters queryParameters
    ) {
        return reviewed.stream().map(r -> fromDto(pageId, r, queryParameters)).collect(Collectors.toUnmodifiableList());
    }

    private ReviewedReplacement fromDto(
        int pageId,
        ReviewedReplacementDto reviewed,
        CommonQueryParameters queryParameters
    ) {
        return ReviewedReplacement
            .builder()
            .pageId(WikipediaPageId.of(queryParameters.getWikipediaLanguage(), pageId))
            .type(ReplacementType.of(reviewed.getKind(), reviewed.getSubtype()))
            .cs(reviewed.getCs())
            .start(reviewed.getStart())
            .reviewer(queryParameters.getUser())
            .build();
    }
}
