package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.Suggestion;
import es.bvalero.replacer.finder.FinderResult;
import java.util.LinkedList;
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
    ReplacementType type;
    List<Suggestion> suggestions;

    // Overwrite the getter to include the original text as the first option
    public List<Suggestion> getSuggestions() {
        // Use a linked list to remove and rearrange easily
        List<Suggestion> sorted = new LinkedList<>(suggestions);

        // If any of the suggestions matches the original then move it as the first suggestion
        // If not we add it
        boolean originalFound = false;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getText().equals(text)) {
                final Suggestion original = sorted.remove(i);
                sorted.add(0, original);
                originalFound = true;
                break;
            }
        }
        if (!originalFound) {
            sorted.add(0, Suggestion.ofNoReplace(text));
        }

        return sorted;
    }
}
