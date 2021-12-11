package es.bvalero.replacer.common.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Enumerates the kinds (or base types) of replacements supported in the application */
@Getter
@AllArgsConstructor
public enum ReplacementKind {
    MISSPELLING_SIMPLE("Ortografía"),
    MISSPELLING_COMPOSED("Compuestos"),
    CUSTOM("Personalizado"),
    DATE("Fechas"),
    EMPTY("");

    // TODO: Remove EMPTY when dummy replacements are removed from application

    private static final Map<String, ReplacementKind> map = Arrays
        .stream(ReplacementKind.values())
        .collect(Collectors.toUnmodifiableMap(ReplacementKind::getLabel, Function.identity()));

    @JsonValue
    private final String label;

    // We cannot override the static method "valueOf(String)"
    // This is needed for mapping from database
    public static ReplacementKind valueOfLabel(String label) {
        if (map.containsKey(label)) {
            return map.get(label);
        } else {
            throw new IllegalArgumentException("Wrong replacement kind label: " + label);
        }
    }

    @Override
    public String toString() {
        return this.label;
    }
}
