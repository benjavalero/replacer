package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.finder.common.FinderResult;
import java.util.List;
import lombok.*;

/**
 * A <strong>replacement</strong> is a potential issue to be checked and fixed (replaced). For instance,
 * the word "aproximated" is misspelled and therefore could be proposed to be replaced with "approximated".
 *
 * Note the importance of the <em>potential</em> adjective, as an issue could be just a false positive.
 * For instance, in Spanish the word "Paris" could be misspelled if it corresponds to the French city
 * (written correctly as "París"), but it would be correct if it refers to the mythological Trojan prince.
 */
@Value
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Replacement implements FinderResult {

    // Two replacements are equal if they have the same start and end
    @EqualsAndHashCode.Include
    int start;

    @EqualsAndHashCode.Include
    String text;

    String type;

    @With(AccessLevel.PACKAGE)
    String subtype;

    @With(AccessLevel.PACKAGE)
    List<Suggestion> suggestions;
}
