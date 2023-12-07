package es.bvalero.replacer.page.find;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.user.User;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * A container for the options of a review. There are 3 possibilities:
 * <ol>
 *     <li>No type. All the fields are empty.</li>
 *     <li>Standard type. Only kind and subtype are specified.</li>
 *     <li>Custom type. All fields are specified.</li>
 * </ol>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
class ReviewOptions {

    @Getter
    @NonNull
    User user;

    @Getter
    @NonNull
    private ReplacementKind kind;

    @Nullable
    private String subtype;

    @Nullable
    private Boolean cs;

    @Nullable
    private String suggestion;

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
        return new ReviewOptions(user, ReplacementKind.EMPTY, null, null, null);
    }

    private static ReviewOptions ofType(User user, byte kind, String subtype) {
        return new ReviewOptions(user, ReplacementKind.valueOf(kind), subtype, null, null);
    }

    @VisibleForTesting
    public static ReviewOptions ofType(User user, StandardType type) {
        return ReviewOptions.ofType(user, type.getKind().getCode(), type.getSubtype());
    }

    @VisibleForTesting
    public static ReviewOptions ofCustom(User user, String replacement, boolean caseSensitive, String suggestion) {
        return new ReviewOptions(user, ReplacementKind.CUSTOM, replacement, caseSensitive, suggestion);
    }

    private static boolean validateCustomSuggestion(String subtype, String suggestion, boolean caseSensitive) {
        if (caseSensitive) {
            return !subtype.equals(suggestion);
        } else {
            return !subtype.equalsIgnoreCase(suggestion);
        }
    }

    public StandardType getStandardType() {
        assert this.kind != ReplacementKind.EMPTY;
        assert this.kind != ReplacementKind.CUSTOM;
        assert this.subtype != null;
        return StandardType.of(this.kind, this.subtype);
    }

    public CustomReplacementFindRequest getCustomReplacementFindRequest() {
        assert this.kind == ReplacementKind.CUSTOM;
        assert this.subtype != null;
        assert this.cs != null;
        assert this.suggestion != null;
        return CustomReplacementFindRequest.of(this.subtype, this.cs, this.suggestion);
    }

    public CustomType getCustomType() {
        assert this.kind == ReplacementKind.CUSTOM;
        assert this.subtype != null;
        assert this.cs != null;
        assert this.suggestion != null;
        return CustomType.of(this.subtype, this.cs);
    }

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();
        list.add(this.kind.toString());
        if (this.subtype != null) {
            list.add(this.subtype);
        }
        if (this.cs != null) {
            list.add(Boolean.toString(this.cs));
        }
        if (this.suggestion != null) {
            list.add(this.suggestion);
        }
        return StringUtils.join(list, " - ");
    }
}
