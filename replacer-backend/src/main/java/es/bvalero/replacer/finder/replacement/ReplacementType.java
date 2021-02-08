package es.bvalero.replacer.finder.replacement;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReplacementType {

    // TODO: This might by an enumerate. Should it include also the known subtypes?
    public static final String MISSPELLING_SIMPLE = "Ortografía";
    public static final String MISSPELLING_COMPOSED = "Compuestos";
    public static final String CUSTOM = "Personalizado";
}
