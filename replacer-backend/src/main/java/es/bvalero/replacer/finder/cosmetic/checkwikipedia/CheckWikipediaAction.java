package es.bvalero.replacer.finder.cosmetic.checkwikipedia;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CheckWikipediaAction {
    TEMPLATE_WORD_USELESS(1),
    BREAK_INCORRECT_SYNTAX(2),
    DEFAULT_SORT_SPECIAL_CHARACTERS(6),
    CATEGORY_IN_ENGLISH(21),
    CATEGORY_WITH_WHITESPACE(22),
    HTML_DASH(50),
    BREAK_IN_LIST(54),
    LINK_EQUAL_TO_LINK_TEXT(64);

    private final int value;
}
