package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.user.User;
import lombok.Value;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Value(staticConstructor = "of")
class ReviewOptions {

    @NonNull
    User user;

    @NonNull
    ReplacementType type;

    static ReviewOptions of(
        User user,
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
            return ofNoType(user);
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
            return ofCustom(user, subtype, cs, suggestion);
        } else {
            // Standard type
            if (subtype == null || suggestion != null || cs != null) {
                throw new IllegalArgumentException("Non-null custom options for a standard kind");
            }
            return ofType(user, kind, subtype);
        }
    }

    static ReviewOptions ofNoType(User user) {
        return ReviewOptions.of(user, ReplacementType.NO_TYPE);
    }

    private static ReviewOptions ofType(User user, byte kind, String subtype) {
        return ReviewOptions.of(user, StandardType.of(kind, subtype));
    }

    @VisibleForTesting
    public static ReviewOptions ofType(User user, StandardType standardType) {
        return ReviewOptions.of(user, standardType);
    }

    private static ReviewOptions ofCustom(User user, String replacement, boolean caseSensitive, String suggestion) {
        return ReviewOptions.of(user, CustomType.of(replacement, caseSensitive, suggestion));
    }

    @VisibleForTesting
    public static ReviewOptions ofCustom(User user, CustomType customType) {
        return ReviewOptions.of(user, customType);
    }

    @Override
    public String toString() {
        return String.format("%s - %s", this.user, this.type);
    }

    private static boolean validateCustomSuggestion(String subtype, String suggestion, boolean caseSensitive) {
        if (caseSensitive) {
            return !subtype.equals(suggestion);
        } else {
            return !subtype.equalsIgnoreCase(suggestion);
        }
    }
}
