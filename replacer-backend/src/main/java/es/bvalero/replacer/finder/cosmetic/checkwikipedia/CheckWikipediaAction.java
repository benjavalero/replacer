package es.bvalero.replacer.finder.cosmetic.checkwikipedia;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CheckWikipediaAction {
    LINK_EQUAL_TO_LINKTEXT(64);

    private final int value;
}
