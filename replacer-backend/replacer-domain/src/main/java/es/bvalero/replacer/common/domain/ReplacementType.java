package es.bvalero.replacer.common.domain;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

/** Type of replacement found in the content of a page */
@Value
public class ReplacementType {

    public static final ReplacementType NO_TYPE = ofNoType();
    public static final ReplacementType DATE = ReplacementType.of(ReplacementKind.STYLE, "Fechas");
    public static final ReplacementType ACUTE_O = ReplacementType.of(ReplacementKind.STYLE, "ó con tilde");

    public static final ReplacementType CENTURY = ReplacementType.of(ReplacementKind.STYLE, "Siglo sin versalitas");
    public static final ReplacementType COORDINATES = ReplacementType.of(ReplacementKind.STYLE, "Coordenadas");
    public static final ReplacementType DEGREES = ReplacementType.of(ReplacementKind.STYLE, "Grados");

    public static final int MAX_SUBTYPE_LENGTH = 100; // Constrained by the database

    @NonNull
    ReplacementKind kind;

    @NonNull
    String subtype;

    private static ReplacementType ofNoType() {
        return new ReplacementType(ReplacementKind.EMPTY, EMPTY);
    }

    public static ReplacementType of(byte kind, String subtype) {
        ReplacementKind replacementKind = ReplacementKind.valueOf(kind);
        return replacementKind == ReplacementKind.CUSTOM
            ? ReplacementType.ofCustom(subtype)
            : ReplacementType.of(replacementKind, subtype);
    }

    public static ReplacementType of(ReplacementKind kind, String subtype) {
        if (kind == ReplacementKind.EMPTY || kind == ReplacementKind.CUSTOM) {
            throw new IllegalArgumentException("Invalid kind for a standard type: " + kind);
        }

        validateSubtype(subtype);

        return new ReplacementType(kind, subtype);
    }

    public static ReplacementType ofCustom(String replacement) {
        validateSubtype(replacement);
        return new ReplacementType(ReplacementKind.CUSTOM, replacement);
    }

    private static void validateSubtype(String subtype) {
        if (StringUtils.isBlank(subtype)) {
            throw new IllegalArgumentException("Invalid blank subtype for a standard type");
        }

        if (subtype.length() > MAX_SUBTYPE_LENGTH) {
            throw new IllegalArgumentException("Too long subtype: " + subtype);
        }
    }

    public boolean isStandardType() {
        return !this.equals(NO_TYPE) && !isCustomType();
    }

    private boolean isCustomType() {
        return this.kind == ReplacementKind.CUSTOM;
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
        if (this.equals(NO_TYPE)) {
            return "NO TYPE";
        } else {
            return String.format("%s - %s", this.kind, this.subtype);
        }
    }
}
