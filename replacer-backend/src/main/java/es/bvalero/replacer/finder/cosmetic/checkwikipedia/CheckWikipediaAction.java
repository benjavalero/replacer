package es.bvalero.replacer.finder.cosmetic.checkwikipedia;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CheckWikipediaAction {
    TEMPLATE_CONTAINS_USELESS_WORD_TEMPLATE(1),
    LINK_EQUAL_TO_LINK_TEXT(64);

    private final int value;
}
