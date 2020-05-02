package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WikipediaLanguage {
    SPANISH("es"),
    GALICIAN("gl"),
    ALL(""),;

    private static final Map<String, WikipediaLanguage> map = Arrays
        .stream(WikipediaLanguage.values())
        .collect(Collectors.toMap(WikipediaLanguage::getCode, Function.identity()));

    @JsonValue
    private final String code;

    @JsonCreator
    public static WikipediaLanguage forValues(String code) {
        return map.get(code);
    }
}
