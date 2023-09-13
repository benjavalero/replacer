package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.wikipedia.WikipediaSection;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

@UtilityClass
class ReviewMapper {

    ReviewPage toDto(Review review) {
        return ReviewPage
            .builder()
            .lang(review.getPage().getPageKey().getLang().getCode())
            .pageId(review.getPage().getPageKey().getPageId())
            .title(review.getPage().getTitle())
            .content(review.getPage().getContent())
            .section(toDto(review.getSection()))
            .queryTimestamp(review.getPage().getQueryTimestamp().toString())
            .replacements(toDto(review.getReplacements()))
            .numPending(review.getNumPending())
            .build();
    }

    @Nullable
    private ReviewSection toDto(@Nullable WikipediaSection section) {
        if (section == null) {
            return null;
        } else {
            return ReviewSection.of(section.getIndex(), section.getAnchor(), section.getByteOffset());
        }
    }

    private Collection<ReviewReplacement> toDto(Collection<Replacement> replacements) {
        return replacements.stream().map(ReviewMapper::toDto).collect(Collectors.toUnmodifiableList());
    }

    private ReviewReplacement toDto(Replacement replacement) {
        Boolean caseSensitive = replacement.getType() instanceof CustomType
            ? ((CustomType) replacement.getType()).isCaseSensitive()
            : null;
        return ReviewReplacement.of(
            replacement.getStart(),
            replacement.getText(),
            replacement.getType().getKind().getCode(),
            replacement.getType().getSubtype(),
            caseSensitive,
            replacement.getSuggestions().stream().map(ReviewMapper::toDto).collect(Collectors.toList())
        );
    }

    private ReviewSuggestion toDto(Suggestion suggestion) {
        return ReviewSuggestion.of(suggestion.getText(), suggestion.getComment());
    }

    ReviewOptions fromDto(ReviewOptionsDto options, User user) {
        return ReviewOptions.of(
            user,
            options.getKind(),
            options.getSubtype(),
            options.getCs(),
            options.getSuggestion()
        );
    }
}
