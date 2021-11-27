package es.bvalero.replacer.common.domain;

import es.bvalero.replacer.finder.replacement.ReplacementType;
import java.util.Collection;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Domain object representing a replacement found in the content of a page.
 *
 * A <strong>replacement</strong> is a potential issue to be checked and fixed (replaced). For instance,
 * the word "aproximated" is misspelled and therefore could be proposed to be replaced with "approximated".
 *
 * Note the importance of the <em>potential</em> adjective, as an issue could be just a false positive.
 * For instance, in Spanish the word "Paris" could be misspelled if it corresponds to the French city
 * (written correctly as "París"), but it would be correct if it refers to the mythological Trojan prince.
 */
@Value
@Builder
public class Replacement {

    @NonNull
    Integer start;

    @NonNull
    String text;

    @NonNull
    ReplacementType type;

    @NonNull
    String subtype;

    @NonNull
    Collection<Suggestion> suggestions;

    public int getEnd() {
        return this.getStart() + this.getText().length();
    }
}
