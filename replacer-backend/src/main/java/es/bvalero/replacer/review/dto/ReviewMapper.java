package es.bvalero.replacer.review.dto;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.util.WikipediaDateUtils;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

@UtilityClass
public class ReviewMapper {

    public FindReviewResponse toDto(Review review, ReviewOptions options) {
        return FindReviewResponse.of(
            toDto(review.getPage(), review.getSection()),
            toDto(review.getReplacements()),
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

    private Collection<ReviewReplacementDto> toDto(Collection<Replacement> replacements) {
        return replacements.stream().map(ReviewMapper::toDto).collect(Collectors.toUnmodifiableList());
    }

    private ReviewReplacementDto toDto(Replacement replacement) {
        return ReviewReplacementDto.of(
            replacement.getStart(),
            replacement.getText(),
            replacement.getType().getKind().getCode(),
            replacement.getType().getSubtype(),
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

    public ReviewOptions fromDto(
        ReviewOptionsDto options,
        boolean reviewAllTypes,
        CommonQueryParameters queryParameters
    ) {
        return ReviewOptions
            .builder()
            .lang(WikipediaLanguage.valueOfCode(queryParameters.getLang()))
            .user(queryParameters.getUser())
            .type(ReplacementType.of(options.getKind(), options.getSubtype()))
            .suggestion(options.getSuggestion())
            .cs(options.getCs())
            .reviewAllTypes(reviewAllTypes)
            .build();
    }
}
