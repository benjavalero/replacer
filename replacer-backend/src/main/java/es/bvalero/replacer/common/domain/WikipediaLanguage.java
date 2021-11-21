package es.bvalero.replacer.common.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.TestOnly;

/** Domain value object. It enumerates the Wikipedia languages supported by the application. */
@Getter
@AllArgsConstructor
public enum WikipediaLanguage {
    GALICIAN("gl"),
    SPANISH("es");

    private static final Map<String, WikipediaLanguage> map = Arrays
        .stream(WikipediaLanguage.values())
        .collect(Collectors.toMap(WikipediaLanguage::getCode, Function.identity()));

    @JsonValue
    private final String code;

    // We choose a default language to be used on unit tests
    @TestOnly
    public static WikipediaLanguage getDefault() {
        return SPANISH;
    }

    // We cannot override the static method "valueOf(String)"
    // This is needed for WikipediaLanguageConverter
    static WikipediaLanguage valueOfCode(String code) {
        if (map.containsKey(code)) {
            return map.get(code);
        } else {
            throw new IllegalArgumentException("Wrong language code: " + code);
        }
    }

    @Override
    public String toString() {
        return this.code;
    }
}
