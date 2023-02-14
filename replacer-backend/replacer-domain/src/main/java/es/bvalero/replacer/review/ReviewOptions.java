package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.user.UserId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Value
@Builder(access = AccessLevel.PRIVATE)
class ReviewOptions {

    @NonNull
    UserId userId;

    @NonNull
    ReplacementType type;

    @Nullable
    String suggestion;

    @Nullable
    Boolean cs;

    static ReviewOptions of(
        UserId userId,
        @Nullable Byte kind,
        @Nullable String subtype,
        @Nullable String suggestion,
        @Nullable Boolean cs
    ) {
        if (kind == null) {
            // No type
            if (subtype != null || suggestion != null || cs != null) {
                throw new IllegalArgumentException("Non-null options for a no-type kind");
            }
            return ofNoType(userId);
        } else if (kind == ReplacementKind.CUSTOM.getCode()) {
            // Custom type
            if (
                subtype == null ||
                suggestion == null ||
                cs == null ||
                !validateCustomSuggestion(subtype, suggestion, cs)
            ) {
                throw new IllegalArgumentException("Null custom options for a custom kind");
            }
            return ofCustom(userId, subtype, suggestion, cs);
        } else {
            // Standard type
            if (subtype == null || suggestion != null || cs != null) {
                throw new IllegalArgumentException("Non-null custom options for a standard kind");
            }
            return ofType(userId, kind, subtype);
        }
    }

    static ReviewOptions ofNoType(UserId userId) {
        return ReviewOptions.builder().userId(userId).type(ReplacementType.NO_TYPE).build();
    }

    public static ReviewOptions ofType(UserId userId, Byte kind, String subtype) {
        return ReviewOptions.builder().userId(userId).type(ReplacementType.of(kind, subtype)).build();
    }

    public static ReviewOptions ofCustom(UserId userId, String replacement, String suggestion, boolean caseSensitive) {
        return ReviewOptions
            .builder()
            .userId(userId)
            .type(ReplacementType.ofCustom(replacement))
            .suggestion(suggestion)
            .cs(caseSensitive)
            .build();
    }

    ReviewOptionsType getOptionsType() {
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
        return String.format("%s - %s", this.userId, toStringSearchType());
    }

    String toStringSearchType() {
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

    private static boolean validateCustomSuggestion(String subtype, String suggestion, boolean caseSensitive) {
        if (caseSensitive) {
            return !subtype.equals(suggestion);
        } else {
            return !subtype.equalsIgnoreCase(suggestion);
        }
    }
}
