package es.bvalero.replacer.common.dto;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO enumerating the language codes the application may receive in the controllers.
 * The listed values must match with the ones in {@link WikipediaLanguage}
 */
@Schema(enumAsRef = true, description = "Language of the Wikipedia in use", example = "es")
public enum Language {
    gl,
    es;

    public static Language of(WikipediaLanguage wikipediaLanguage) {
        return Language.valueOf(wikipediaLanguage.getCode());
    }

    public WikipediaLanguage toDomain() {
        return WikipediaLanguage.valueOfCode(this.name());
    }
}
