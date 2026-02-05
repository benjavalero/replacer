package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.User;
import es.bvalero.replacer.finder.CustomType;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.wikipedia.WikipediaSection;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;

class ReviewMapper {

    static Page toDto(Review review) {
        return Page.builder()
            .lang(review.getPage().getPageKey().getLang().getCode())
            .pageId(review.getPage().getPageKey().getPageId())
            .title(review.getPage().getTitle())
            .content(review.getPage().getContent())
            .section(toDto(review.getSection()))
            .queryTimestamp(review.getPage().getQueryTimestamp().toString())
            .replacements(toDto(review.getReplacements()))
            .build();
    }

    @Nullable
    private static Section toDto(@Nullable WikipediaSection section) {
        if (section == null) {
            return null;
        } else {
            return Section.of(section.getIndex(), section.getAnchor(), section.getByteOffset());
        }
    }

    private static Collection<ReplacementDto> toDto(Collection<Replacement> replacements) {
        return replacements.stream().map(ReviewMapper::toDto).toList();
    }

    private static ReplacementDto toDto(Replacement replacement) {
        Boolean caseSensitive = replacement.type() instanceof CustomType customType
            ? customType.isCaseSensitive()
            : null;
        return ReplacementDto.of(
            replacement.start(),
            replacement.text(),
            replacement.type().getKind().getCode(),
            replacement.type().getSubtype(),
            caseSensitive,
            replacement.suggestions().stream().map(ReviewMapper::toDto).collect(Collectors.toList())
        );
    }

    private static SuggestionDto toDto(Suggestion suggestion) {
        return SuggestionDto.of(suggestion.getText(), suggestion.getComment());
    }

    static ReviewOptions fromDto(ReviewOptionsDto options, User user) {
        return ReviewOptions.of(
            user,
            options.getKind(),
            options.getSubtype(),
            options.getCs(),
            options.getSuggestion()
        );
    }
}
