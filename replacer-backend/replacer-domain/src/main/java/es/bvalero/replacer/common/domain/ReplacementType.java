package es.bvalero.replacer.common.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@EqualsAndHashCode
public abstract class ReplacementType {

    private static final int MAX_SUBTYPE_LENGTH = 100; // Constrained by the database

    private final ReplacementKind kind;
    private final String subtype;

    ReplacementType(ReplacementKind kind, String subtype) {
        validateSubtype(subtype);

        this.kind = kind;
        this.subtype = subtype;
    }

    private void validateSubtype(String subtype) {
        if (StringUtils.isBlank(subtype)) {
            throw new IllegalArgumentException("Invalid blank subtype for a standard type");
        }

        if (subtype.length() > MAX_SUBTYPE_LENGTH) {
            throw new IllegalArgumentException("Too long subtype: " + subtype);
        }
    }
}
