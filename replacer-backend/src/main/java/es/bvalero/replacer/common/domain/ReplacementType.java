package es.bvalero.replacer.common.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Enumerates the base types of replacements supported in the application */
@Getter
@AllArgsConstructor
public enum ReplacementType {
    MISSPELLING_SIMPLE("Ortografía"),
    MISSPELLING_COMPOSED("Compuestos"),
    CUSTOM("Personalizado"),
    DATE("Fechas");

    @JsonValue
    private final String label;

    @Override
    public String toString() {
        return this.label;
    }

    // TODO: Move as domain should not be aware of Misspelling class
    public static ReplacementType ofMisspellingType(Misspelling misspelling) {
        if (misspelling instanceof SimpleMisspelling) {
            return MISSPELLING_SIMPLE;
        } else if (misspelling instanceof ComposedMisspelling) {
            return MISSPELLING_COMPOSED;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
