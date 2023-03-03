package es.bvalero.replacer.common.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.lang.NonNull;

/** Type of replacement found in the content of a page */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Override the default constructor to constrain the access
public class StandardType implements ReplacementType {

    public static final StandardType DATE = of(ReplacementKind.STYLE, "Fechas");
    public static final StandardType ACUTE_O = of(ReplacementKind.STYLE, "ó con tilde");

    public static final StandardType CENTURY = of(ReplacementKind.STYLE, "Siglo sin versalitas");
    public static final StandardType COORDINATES = of(ReplacementKind.STYLE, "Coordenadas");
    public static final StandardType DEGREES = of(ReplacementKind.STYLE, "Grados");

    @NonNull
    ReplacementKind kind;

    @NonNull
    String subtype;

    public static StandardType of(ReplacementKind kind, String subtype) {
        if (kind == ReplacementKind.EMPTY || kind == ReplacementKind.CUSTOM) {
            throw new IllegalArgumentException("Invalid kind for a standard type: " + kind);
        }

        ReplacementType.validateSubtype(subtype);

        return new StandardType(kind, subtype);
    }

    public static StandardType of(byte kind, String subtype) {
        return of(ReplacementKind.valueOf(kind), subtype);
    }

    public boolean isForBots() {
        // Note that this class is not an enumerate, so it must be compared with equals.
        return false;
    }

    public boolean isForAdmin() {
        // Note that this class is not an enumerate, so it must be compared with equals.
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", this.kind, this.subtype);
    }
}
