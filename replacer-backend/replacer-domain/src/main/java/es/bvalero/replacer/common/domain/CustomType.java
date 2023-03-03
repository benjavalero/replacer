package es.bvalero.replacer.common.domain;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Override the default constructor to constrain the access
public class CustomType implements ReplacementType {

    @Override
    public ReplacementKind getKind() {
        return ReplacementKind.CUSTOM;
    }

    @NonNull
    String subtype;

    boolean caseSensitive;

    @NonNull
    String suggestion;

    public static CustomType ofReviewed(String replacement, boolean caseSensitive) {
        ReplacementType.validateSubtype(replacement);
        return new CustomType(replacement, caseSensitive, EMPTY);
    }

    public static CustomType of(String replacement, boolean caseSensitive, String suggestion) {
        ReplacementType.validateSubtype(replacement);
        if (StringUtils.isBlank(suggestion)) {
            throw new IllegalArgumentException("Blank custom suggestion");
        }
        return new CustomType(replacement, caseSensitive, suggestion);
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s", this.getKind(), this.subtype, this.caseSensitive);
    }
}
