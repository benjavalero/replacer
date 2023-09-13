package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.user.User;
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
        return ReviewPage
            .builder()
            .lang(page.getPageKey().getLang().getCode())
            .pageId(page.getPageKey().getPageId())
            .title(page.getTitle())
            .content(page.getContent())
            .section(toDto(section))
            .queryTimestamp(page.getQueryTimestamp().toString())
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
