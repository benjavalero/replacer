package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.user.UserId;
import lombok.Value;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Value(staticConstructor = "of")
class ReviewOptions {

    @NonNull
    UserId userId;

    @NonNull
    ReplacementType type;

    static ReviewOptions of(
        UserId userId,
        @Nullable Byte kind,
        @Nullable String subtype,
        @Nullable Boolean cs,
        @Nullable String suggestion
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
            return ofCustom(userId, subtype, cs, suggestion);
        } else {
            // Standard type
            if (subtype == null || suggestion != null || cs != null) {
                throw new IllegalArgumentException("Non-null custom options for a standard kind");
            }
            return ofType(userId, kind, subtype);
        }
    }

    static ReviewOptions ofNoType(UserId userId) {
        return ReviewOptions.of(userId, ReplacementType.NO_TYPE);
    }

    private static ReviewOptions ofType(UserId userId, byte kind, String subtype) {
        return ReviewOptions.of(userId, StandardType.of(kind, subtype));
    }

    @VisibleForTesting
    public static ReviewOptions ofType(UserId userId, StandardType standardType) {
        return ReviewOptions.of(userId, standardType);
    }

    private static ReviewOptions ofCustom(UserId userId, String replacement, boolean caseSensitive, String suggestion) {
        return ReviewOptions.of(userId, CustomType.of(replacement, caseSensitive, suggestion));
    }

    @VisibleForTesting
    public static ReviewOptions ofCustom(UserId userId, CustomType customType) {
        return ReviewOptions.of(userId, customType);
    }

    @Override
    public String toString() {
        return String.format("%s - %s", this.userId, this.type);
    }

    private static boolean validateCustomSuggestion(String subtype, String suggestion, boolean caseSensitive) {
        if (caseSensitive) {
            return !subtype.equals(suggestion);
        } else {
            return !subtype.equalsIgnoreCase(suggestion);
        }
    }
}
