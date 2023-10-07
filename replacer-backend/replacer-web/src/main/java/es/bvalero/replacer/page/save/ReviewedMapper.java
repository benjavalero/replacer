package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.User;
import java.util.Collection;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

@UtilityClass
class ReviewedMapper {

    Collection<ReviewedReplacement> fromDto(
        int pageId,
        Collection<ReviewedReplacementDto> reviewed,
        @Nullable Integer sectionOffset,
        User user
    ) {
        return reviewed.stream().map(r -> fromDto(pageId, r, sectionOffset, user)).toList();
    }

    private ReviewedReplacement fromDto(
        int pageId,
        ReviewedReplacementDto reviewed,
        @Nullable Integer sectionOffset,
        User user
    ) {
        ReplacementKind replacementKind = ReplacementKind.valueOf(reviewed.getKind());
        ReplacementType replacementType = replacementKind == ReplacementKind.CUSTOM
            ? CustomType.of(reviewed.getSubtype(), Objects.requireNonNull(reviewed.getCs()))
            : StandardType.of(replacementKind, reviewed.getSubtype());
        int offset = Objects.requireNonNullElse(sectionOffset, 0);
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
