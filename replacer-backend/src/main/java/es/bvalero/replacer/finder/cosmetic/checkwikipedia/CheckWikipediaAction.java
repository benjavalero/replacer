package es.bvalero.replacer.finder.cosmetic.checkwikipedia;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CheckWikipediaAction {
    TEMPLATE_WORD_USELESS(1),
    BREAK_INCORRECT_SYNTAX(2),
    DEFAULT_SORT_SPECIAL_CHARACTERS(6),
    UNICODE_CONTROL_CHARACTERS(16),
    CATEGORY_IN_LOWERCASE(18),
    CATEGORY_IN_ENGLISH(21),
    CATEGORY_WITH_WHITESPACE(22),
    HEADLINE_BOLD(44),
    BREAK_IN_LIST(54),
    DOUBLE_SMALL_TAG(55),
    HEADLINE_END_WITH_COLON(57),
    LINK_EQUAL_TO_LINK_TEXT(64);

    private final int value;
}
