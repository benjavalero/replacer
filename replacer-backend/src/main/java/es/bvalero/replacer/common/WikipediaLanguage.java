package es.bvalero.replacer.common;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    public static WikipediaLanguage getDefault() {
        return SPANISH;
    }

    // We cannot override the static method "valueOf(String)"
    static WikipediaLanguage valueOfCode(String code) {
        if (map.containsKey(code)) {
            return map.get(code);
        } else {
            String msg = String.format("Wrong language code: %s", code);
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public String toString() {
        return this.code;
    }
}
