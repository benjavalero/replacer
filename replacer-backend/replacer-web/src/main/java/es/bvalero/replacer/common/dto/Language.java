package es.bvalero.replacer.common.dto;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO enumerating the language codes the application may receive in the controllers.
 * The listed values must match with the ones in {@link WikipediaLanguage}
 */
@Schema(enumAsRef = true)
public enum Language {
    gl,
    es,
}
