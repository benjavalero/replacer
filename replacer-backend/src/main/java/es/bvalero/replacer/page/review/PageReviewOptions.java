package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Value
@Builder(access = AccessLevel.PACKAGE)
public class PageReviewOptions {

    @NonNull
    WikipediaLanguage lang;

    @NonNull
    String user;

    @NonNull
    ReplacementType type;

    @Nullable
    String suggestion;

    @Nullable
    Boolean cs;

    // Mark as reviewed all page replacements despite the type in the options
    boolean reviewAllTypes;

    @TestOnly
    public static PageReviewOptions ofNoType() {
        return PageReviewOptions
            .builder()
            .lang(WikipediaLanguage.getDefault())
            .user("A")
            .type(ReplacementType.ofEmpty())
            .reviewAllTypes(false)
            .build();
    }

    @TestOnly
    public static PageReviewOptions ofType(ReplacementType type) {
        return PageReviewOptions
            .builder()
            .lang(WikipediaLanguage.getDefault())
            .user("A")
            .type(type)
            .reviewAllTypes(false)
            .build();
    }

    @TestOnly
    static PageReviewOptions ofCustom(
        WikipediaLanguage lang,
        String replacement,
        String suggestion,
        boolean caseSensitive
    ) {
        return PageReviewOptions
            .builder()
            .lang(lang)
            .user("A")
            .type(ReplacementType.of(ReplacementKind.CUSTOM, replacement))
            .suggestion(suggestion)
            .cs(caseSensitive)
            .reviewAllTypes(false)
            .build();
    }

    public PageReviewOptionsType getOptionsType() {
        switch (type.getKind()) {
            case EMPTY:
                return PageReviewOptionsType.NO_TYPE;
            case CUSTOM:
                return PageReviewOptionsType.CUSTOM;
            default:
                return PageReviewOptionsType.TYPE_SUBTYPE;
        }
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s", user, lang, toStringSearchType());
    }

    String toStringSearchType() {
        List<String> list = new ArrayList<>();
        switch (getOptionsType()) {
            case NO_TYPE:
                list.add("NO TYPE");
                break;
            case TYPE_SUBTYPE:
                list.add(type.toString());
                break;
            case CUSTOM:
                list.add(type.toString());
                list.add(suggestion);
                list.add(Boolean.toString(Objects.requireNonNull(cs)));
                break;
        }

        return StringUtils.join(list, " - ");
    }

    // Used by the builder
    PageReviewOptions(
        WikipediaLanguage lang,
        String user,
        ReplacementType type,
        @Nullable String suggestion,
        @Nullable Boolean cs,
        boolean reviewAllTypes
    ) {
        this.lang = lang;
        this.user = user;
        this.type = type;
        this.suggestion = suggestion;
        this.cs = cs;
        this.reviewAllTypes = reviewAllTypes;

        if (!isValid()) {
            throw new IllegalArgumentException("Page Review Options not valid");
        }
    }

    boolean isValid() {
        boolean isValid;
        switch (type.getKind()) {
            case EMPTY:
                isValid = StringUtils.isEmpty(type.getSubtype()) && suggestion == null && cs == null;
                break;
            case CUSTOM:
                isValid =
                    StringUtils.isNotBlank(type.getSubtype()) &&
                    StringUtils.isNotBlank(suggestion) &&
                    cs != null &&
                    validateCustomSuggestion();
                break;
            default:
                isValid = StringUtils.isNotBlank(type.getSubtype()) && suggestion == null && cs == null;
                break;
        }
        return isValid;
    }

    private boolean validateCustomSuggestion() {
        if (Boolean.TRUE.equals(cs)) {
            return !type.getSubtype().equals(suggestion);
        } else {
            return !type.getSubtype().equalsIgnoreCase(suggestion);
        }
    }
}
