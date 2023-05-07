package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaSection;
import java.util.Collection;
import java.util.Objects;
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

    ReviewOptions fromDto(ReviewOptionsDto options, User user) {
        return ReviewOptions.of(
            user,
            options.getKind(),
            options.getSubtype(),
            options.getCs(),
            options.getSuggestion()
        );
    }

    Collection<ReviewedReplacement> fromDto(
        int pageId,
        Collection<ReviewedReplacementDto> reviewed,
        int offset,
        User user
    ) {
        return reviewed.stream().map(r -> fromDto(pageId, r, offset, user)).collect(Collectors.toUnmodifiableList());
    }

    private ReviewedReplacement fromDto(int pageId, ReviewedReplacementDto reviewed, int offset, User user) {
        ReplacementKind replacementKind = ReplacementKind.valueOf(reviewed.getKind());
        ReplacementType replacementType = replacementKind == ReplacementKind.CUSTOM
            ? CustomType.ofReviewed(reviewed.getSubtype(), Objects.requireNonNull(reviewed.getCs()))
            : StandardType.of(replacementKind, reviewed.getSubtype());
        return ReviewedReplacement
            .builder()
            .pageKey(PageKey.of(user.getId().getLang(), pageId))
            .type(replacementType)
            .start(offset + reviewed.getStart())
            .reviewer(user.getId().getUsername())
            .fixed(reviewed.isFixed())
            .build();
    }
}
