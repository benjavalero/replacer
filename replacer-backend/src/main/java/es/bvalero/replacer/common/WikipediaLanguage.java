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

    public static WikipediaLanguage forValues(String code) {
        if (map.containsKey(code)) {
            return map.get(code);
        } else {
            LOGGER.error("Wrong language code: {}", code);
            return getDefault();
        }
    }

    @Override
    public String toString() {
        return this.code;
    }
}
