package es.bvalero.replacer.finder;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.bvalero.replacer.user.User;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Getter;
import org.jetbrains.annotations.TestOnly;

/** Type of replacement found in the content of a page */
@Getter
public class StandardType extends ReplacementType {

    private final boolean forBots;
    private final boolean forAdmin;

    private StandardType(ReplacementKind kind, String subtype, boolean forBots, boolean forAdmin) {
        super(kind, subtype);
        this.forBots = forBots;
        this.forAdmin = forAdmin;
    }

    // Cache the known types to reuse them and save memory
    private static final Cache<String, StandardType> cachedTypes = Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.DAYS)
        .build();

    public static final StandardType DATE = ofStyle("Fechas");
    public static final StandardType ACUTE_O = ofStyle("ó con tilde");
    public static final StandardType CENTURY = ofStyle("Siglo sin versalitas");
    public static final StandardType COORDINATES = ofStyleForBots("Coordenadas");
    public static final StandardType DEGREES = ofStyle("Grados");
    public static final StandardType ORDINAL = ofStyleForBots("Ordinales");

    private static final Set<String> STYLE_SUBTYPES = Set.of(DATE, ACUTE_O, CENTURY, COORDINATES, DEGREES, ORDINAL)
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

        return getTypeFromCache(kind, subtype, false);
    }

    private static StandardType ofStyle(String subtype) {
        return getTypeFromCache(ReplacementKind.STYLE, subtype, false);
    }

    private static StandardType ofStyleForBots(String subtype) {
        return getTypeFromCache(ReplacementKind.STYLE, subtype, true);
    }

    @TestOnly
    public static StandardType ofForAdmin(ReplacementKind kind, String subtype) {
        return new StandardType(kind, subtype, false, true);
    }

    private static StandardType getTypeFromCache(ReplacementKind kind, String subtype, boolean forBots) {
        return Objects.requireNonNull(
            cachedTypes.get(getCacheKey(kind, subtype), k -> new StandardType(kind, subtype, forBots, false))
        );
    }

    private static String getCacheKey(ReplacementKind kind, String subtype) {
        return String.format("%s-%s", kind.getCode(), subtype);
    }

    public static StandardType of(byte kind, String subtype) {
        return of(ReplacementKind.valueOf(kind), subtype);
    }

    public boolean isTypeForbidden(User user) {
        return isForAdmin() && !user.isAdmin();
    }

    @Override
    public String toString() {
        return String.format("%s - %s", this.getKind(), this.getSubtype());
    }
}
