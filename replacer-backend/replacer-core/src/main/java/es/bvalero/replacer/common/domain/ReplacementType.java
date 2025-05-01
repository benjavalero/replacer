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
        // Usually we perform the validations outside the constructor
        // In this case we do it inside so the validation is performed by all the subclasses
        validateSubtype(subtype);

        this.kind = kind;
        this.subtype = subtype;
    }

    private void validateSubtype(String subtype) {
        if (StringUtils.isBlank(subtype)) {
            throw new IllegalArgumentException("Blank subtype");
        }

        if (subtype.length() > MAX_SUBTYPE_LENGTH) {
            throw new IllegalArgumentException("Too long subtype: " + subtype);
        }
    }
}
