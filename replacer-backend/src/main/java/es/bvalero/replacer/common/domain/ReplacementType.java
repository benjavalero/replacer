package es.bvalero.replacer.common.domain;

import lombok.Value;
import org.springframework.lang.NonNull;

/** Type of replacement found in the content of a page */
@Value(staticConstructor = "of")
public class ReplacementType {

    private static final int MAX_SUBTYPE_LENGTH = 100; // Constrained by the database
    public static final ReplacementType EMPTY = ofEmpty();

    @NonNull
    ReplacementKind kind;

    @NonNull
    String subtype;

    private static ReplacementType ofEmpty() {
        return new ReplacementType(ReplacementKind.EMPTY, "");
    }

    private ReplacementType(ReplacementKind kind, String subtype) {
        // Validate subtype
        if (subtype.length() > MAX_SUBTYPE_LENGTH) {
            throw new IllegalArgumentException("Too long replacement subtype: " + subtype);
        }

        this.kind = kind;
        this.subtype = subtype;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", kind.getLabel(), subtype);
    }
}
