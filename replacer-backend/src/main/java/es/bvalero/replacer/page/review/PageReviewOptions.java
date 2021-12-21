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

    @Nullable
    String type; // TODO: ReplacementType

    @Nullable
    String subtype;

    @Nullable
    String suggestion;

    @Nullable
    Boolean cs;

    @Nullable
    public ReplacementType getReplacementType() {
        return Objects.nonNull(type) && Objects.nonNull(subtype) ? ReplacementType.of(type, subtype) : null;
    }

    @TestOnly
    public static PageReviewOptions ofNoType() {
        return PageReviewOptions.builder().lang(WikipediaLanguage.getDefault()).user("A").build();
    }

    @TestOnly
    public static PageReviewOptions ofType(ReplacementType type) {
        return PageReviewOptions
            .builder()
            .lang(WikipediaLanguage.getDefault())
            .user("A")
            .type(type.getKind().getLabel())
            .subtype(type.getSubtype())
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
            .type(ReplacementKind.CUSTOM.getLabel())
            .subtype(replacement)
            .suggestion(suggestion)
            .cs(caseSensitive)
            .build();
    }

    public PageReviewOptionsType getOptionsType() {
        if (StringUtils.isBlank(type)) {
            return PageReviewOptionsType.NO_TYPE;
        } else if (StringUtils.isBlank(suggestion)) {
            return PageReviewOptionsType.TYPE_SUBTYPE;
        } else {
            return PageReviewOptionsType.CUSTOM;
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
                list.add(type);
                list.add(subtype);
                break;
            case CUSTOM:
                list.add(type);
                list.add(subtype);
                list.add(suggestion);
                list.add(Boolean.toString(Objects.requireNonNull(cs)));
                break;
        }

        return StringUtils.join(list, " - ");
    }

    private PageReviewOptions(
        WikipediaLanguage lang,
        String user,
        @Nullable String type,
        @Nullable String subtype,
        @Nullable String suggestion,
        @Nullable Boolean cs
    ) {
        this.lang = lang;
        this.user = user;
        this.type = type;
        this.subtype = subtype;
        this.suggestion = suggestion;
        this.cs = cs;

        if (!isValid()) {
            throw new IllegalArgumentException("Page Review Options not valid");
        }
    }

    boolean isValid() {
        boolean isValid = false;
        switch (getOptionsType()) {
            case NO_TYPE:
                isValid = type == null && subtype == null && suggestion == null && cs == null;
                break;
            case TYPE_SUBTYPE:
                isValid =
                    StringUtils.isNotBlank(type) && StringUtils.isNotBlank(subtype) && suggestion == null && cs == null;
                break;
            case CUSTOM:
                isValid =
                    ReplacementKind.CUSTOM.getLabel().equals(type) &&
                    StringUtils.isNotBlank(subtype) &&
                    StringUtils.isNotBlank(suggestion) &&
                    cs != null;
                break;
        }
        return isValid;
    }
}
