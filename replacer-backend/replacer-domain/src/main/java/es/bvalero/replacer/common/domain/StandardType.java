package es.bvalero.replacer.common.domain;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/** Type of replacement found in the content of a page */
public class StandardType extends ReplacementType {

    private StandardType(ReplacementKind kind, String subtype) {
        super(kind, subtype);
    }

    // Cache the known types to reuse them and save memory
    private static final Cache<String, StandardType> cachedPageIds = Caffeine
        .newBuilder()
        .expireAfterAccess(1, TimeUnit.DAYS)
        .build();

    public static final StandardType DATE = ofStyle("Fechas");
    public static final StandardType ACUTE_O = ofStyle("ó con tilde");
    public static final StandardType CENTURY = ofStyle("Siglo sin versalitas");
    public static final StandardType COORDINATES = ofStyle("Coordenadas");
    public static final StandardType DEGREES = ofStyle("Grados");

    private static final Set<String> STYLE_SUBTYPES = Set
        .of(DATE, ACUTE_O, CENTURY, COORDINATES, DEGREES)
        .stream()
        .map(ReplacementType::getSubtype)
        .collect(Collectors.toUnmodifiableSet());

    public static StandardType of(ReplacementKind kind, String subtype) {
        if (kind == ReplacementKind.EMPTY || kind == ReplacementKind.CUSTOM) {
            throw new IllegalArgumentException("Invalid kind for a standard type: " + kind);
        }

        // Validate style type
        assert STYLE_SUBTYPES != null;
        if (kind == ReplacementKind.STYLE && !STYLE_SUBTYPES.contains(subtype)) {
            throw new IllegalArgumentException("Invalid subtype for a style type: " + subtype);
        }

        return getTypeFromCache(kind, subtype);
    }

    private static StandardType ofStyle(String subtype) {
        return getTypeFromCache(ReplacementKind.STYLE, subtype);
    }

    private static StandardType getTypeFromCache(ReplacementKind kind, String subtype) {
        assert cachedPageIds != null;
        return Objects.requireNonNull(
            cachedPageIds.get(getCacheKey(kind, subtype), k -> new StandardType(kind, subtype))
        );
    }

    private static String getCacheKey(ReplacementKind kind, String subtype) {
        return String.format("%s-%s", kind.getCode(), subtype);
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
        return String.format("%s - %s", this.getKind(), this.getSubtype());
    }
}
