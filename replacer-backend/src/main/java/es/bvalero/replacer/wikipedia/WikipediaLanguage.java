package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WikipediaLanguage {
    SPANISH("es"),
    GALICIAN("gl"),
    ALL("");

    @JsonValue
    private final String code;
}
