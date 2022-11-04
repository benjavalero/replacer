package es.bvalero.replacer.common.domain;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Type of replacement found in the content of a page */
@NonFinal
@Value(staticConstructor = "of")
public class ReplacementType {

    public static final ReplacementType DATE = ReplacementType.of(ReplacementKind.STYLE, "Fechas");
    public static final ReplacementType ACUTE_O = ReplacementType.of(ReplacementKind.STYLE, "ó con tilde");

    public static final ReplacementType CENTURY = ReplacementType.of(ReplacementKind.STYLE, "Siglo sin versalitas");
    public static final ReplacementType COORDINATES = ReplacementType.of(ReplacementKind.STYLE, "Coordenadas");
    public static final ReplacementType DEGREES = ReplacementType.of(ReplacementKind.STYLE, "Grados");

    private static final int MAX_SUBTYPE_LENGTH = 100; // Constrained by the database

    @NonNull
    ReplacementKind kind;

    @NonNull
    String subtype;

    public static ReplacementType of(@Nullable Byte type, @Nullable String subtype) {
        if (type == null && subtype == null) {
            return ofEmpty();
        } else if (type == null || subtype == null) {
            throw new IllegalArgumentException();
        } else {
            return ReplacementType.of(ReplacementKind.valueOf(type), subtype);
        }
    }

    public static ReplacementType ofEmpty() {
        return new ReplacementType(ReplacementKind.EMPTY, "");
    }

    private ReplacementType(ReplacementKind kind, String subtype) {
        // Validate subtype
        if (subtype.length() > MAX_SUBTYPE_LENGTH) {
            throw new IllegalArgumentException("Too long replacement subtype: " + subtype);
        }
        if (kind == ReplacementKind.EMPTY) {
            if (StringUtils.isNotBlank(subtype)) {
                throw new IllegalArgumentException("Non-empty subtype for an empty type: " + subtype);
            }
        } else {
            if (StringUtils.isBlank(subtype)) {
                throw new IllegalArgumentException("Empty subtype for a non-empty type");
            }
        }

        this.kind = kind;
        this.subtype = subtype;
    }

    public boolean isForBots() {
        // For the moment we hardcode this property in the very domain entity
        // Note that this class is not an enumerate, so it must be compared with equals.
        return this.equals(DEGREES);
    }

    @Override
    public String toString() {
        return String.format("%s - %s", kind, subtype);
    }
}
