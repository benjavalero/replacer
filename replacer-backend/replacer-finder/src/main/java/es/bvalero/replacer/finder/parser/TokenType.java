package es.bvalero.replacer.finder.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
enum TokenType {
    TEXT(""),

    START_COMMENT("<!--"),
    END_COMMENT("-->");

    private final String literal;
}
