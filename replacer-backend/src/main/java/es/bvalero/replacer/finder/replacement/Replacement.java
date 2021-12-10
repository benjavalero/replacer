package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.finder.FinderResult;
import java.util.List;
import lombok.Builder;
import lombok.Value;

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
public class Replacement implements FinderResult {

    int start;
    String text;
    ReplacementKind type;
    String subtype;
    List<ReplacementSuggestion> suggestions;
}
