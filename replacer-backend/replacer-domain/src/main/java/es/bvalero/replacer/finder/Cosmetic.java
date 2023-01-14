package es.bvalero.replacer.finder;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.lang.NonNull;

/**
 * A <strong>cosmetic</strong> is a special type of replacement which can be applied automatically,
 * concerning cosmetic modifications, visible or not, e.g. replacing <code>[[Asia|Asia]]</code>
 * by <code>[[Asia]]</code>.
 */
@Value
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cosmetic implements FinderResult {

    @EqualsAndHashCode.Include
    int start;

    @NonNull
    @EqualsAndHashCode.Include
    String text;

    @NonNull
    String fix;

    @Builder.Default
    CheckWikipediaAction checkWikipediaAction = CheckWikipediaAction.NO_ACTION;
}
