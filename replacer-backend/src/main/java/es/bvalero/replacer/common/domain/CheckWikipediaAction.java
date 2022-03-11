package es.bvalero.replacer.common.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Enumerates all actions on Check Wikipedia supported by the application */
@Getter
@AllArgsConstructor
public enum CheckWikipediaAction {
    NO_ACTION(0),

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
    SMALL_TAG_UNNECESSARY(63),
    LINK_EQUAL_TO_LINK_TEXT(64),
    TAG_WITH_NO_CONTENT(85),
    EXTERNAL_LINK_WITH_DOUBLE_HTTP(93);

    private final int value;
}
