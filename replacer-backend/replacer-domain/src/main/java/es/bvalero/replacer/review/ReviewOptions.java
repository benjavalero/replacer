package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.bvalero.replacer.user.UserId;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Value
@Builder
class ReviewOptions {

    @NonNull
    UserId userId;

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
            .userId(UserId.of(WikipediaLanguage.getDefault(),
            "A"))
            .type(ReplacementType.ofEmpty())
            .build();
    }

    @TestOnly
    public static ReviewOptions ofType(ReplacementType type) {
        return ReviewOptions.builder().userId(UserId.of(WikipediaLanguage.getDefault(), "A")).type(type).build();
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
            .userId(UserId.of(lang, "A"))
            .type(ReplacementType.of(ReplacementKind.CUSTOM, replacement))
            .suggestion(suggestion)
            .cs(caseSensitive)
            .build();
    }

    public ReviewOptionsType getOptionsType() {
        switch (this.type.getKind()) {
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
        return String.format("%s - %s - %s", this.userId.getLang(), this.userId.getUsername(), toStringSearchType());
    }

    public String toStringSearchType() {
        List<String> list = new ArrayList<>();
        list.add(this.userId.getLang().getCode());
        switch (getOptionsType()) {
            case NO_TYPE:
                list.add("NO TYPE");
                break;
            case TYPE_SUBTYPE:
                list.add(this.type.toString());
                break;
            case CUSTOM:
                list.add(this.type.toString());
                list.add(this.suggestion);
                list.add(Boolean.toString(Objects.requireNonNull(this.cs)));
                break;
        }

        return StringUtils.join(list, " - ");
    }

    // Used by the builder
    ReviewOptions(
        UserId userId,
        ReplacementType type,
        @Nullable String suggestion,
        @Nullable Boolean cs
    ) {
        this.userId = userId;
        this.type = type;
        this.suggestion = suggestion;
        this.cs = cs;

        if (!isValid()) {
            throw new IllegalArgumentException("Page Review Options not valid");
        }
    }

    boolean isValid() {
        boolean isValid;
        switch (this.type.getKind()) {
            case EMPTY:
                isValid = StringUtils.isEmpty(this.type.getSubtype()) && this.suggestion == null && this.cs == null;
                break;
            case CUSTOM:
                isValid =
                    StringUtils.isNotBlank(this.type.getSubtype()) &&
                    StringUtils.isNotBlank(this.suggestion) &&
                    this.cs != null &&
                    validateCustomSuggestion();
                break;
            default:
                isValid = StringUtils.isNotBlank(this.type.getSubtype()) && this.suggestion == null && this.cs == null;
                break;
        }
        return isValid;
    }

    private boolean validateCustomSuggestion() {
        if (Boolean.TRUE.equals(this.cs)) {
            return !this.type.getSubtype().equals(this.suggestion);
        } else {
            return !this.type.getSubtype().equalsIgnoreCase(this.suggestion);
        }
    }
}
