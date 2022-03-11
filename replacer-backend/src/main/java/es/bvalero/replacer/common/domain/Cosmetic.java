package es.bvalero.replacer.common.domain;

import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;

/**
 * A <strong>cosmetic</strong> is a special type of replacement which can be applied automatically,
 * concerning cosmetic modifications, visible or not, e.g. replacing <code>[[Asia|Asia]]</code>
 * by <code>[[Asia]]</code>.
 */
@Value
@Builder
public class Cosmetic implements FinderResult {

    int start;

    @NonNull
    String text;

    @NonNull
    String fix;

    @Builder.Default
    CheckWikipediaAction checkWikipediaAction = CheckWikipediaAction.NO_ACTION;
}
