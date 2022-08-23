package es.bvalero.replacer.common.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Value
@Builder
public class ReviewOptions {

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

    @TestOnly
    public static ReviewOptions ofNoType() {
        return ReviewOptions
            .builder()
            .lang(WikipediaLanguage.getDefault())
            .user("A")
            .type(ReplacementType.ofEmpty())
            .build();
    }

    @TestOnly
    public static ReviewOptions ofType(ReplacementType type) {
        return ReviewOptions.builder().lang(WikipediaLanguage.getDefault()).user("A").type(type).build();
    }

    @TestOnly
    public static ReviewOptions ofCustom(
        WikipediaLanguage lang,
        String replacement,
        String suggestion,
        boolean caseSensitive
    ) {
        return ReviewOptions
            .builder()
            .lang(lang)
            .user("A")
            .type(ReplacementType.of(ReplacementKind.CUSTOM, replacement))
            .suggestion(suggestion)
            .cs(caseSensitive)
            .build();
    }

    public ReviewOptionsType getOptionsType() {
        switch (type.getKind()) {
            case EMPTY:
                return ReviewOptionsType.NO_TYPE;
            case CUSTOM:
                return ReviewOptionsType.CUSTOM;
            default:
                return ReviewOptionsType.TYPE_SUBTYPE;
        }
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s", lang, user, toStringSearchType());
    }

    public String toStringSearchType() {
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
    ReviewOptions(
        WikipediaLanguage lang,
        String user,
        ReplacementType type,
        @Nullable String suggestion,
        @Nullable Boolean cs
    ) {
        this.lang = lang;
        this.user = user;
        this.type = type;
        this.suggestion = suggestion;
        this.cs = cs;

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
