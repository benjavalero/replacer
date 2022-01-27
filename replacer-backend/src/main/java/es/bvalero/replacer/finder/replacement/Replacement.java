package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.Suggestion;
import es.bvalero.replacer.finder.FinderResult;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
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
        Collection<Suggestion> merged = mergeSuggestions(this.suggestions);
        return sortSuggestions(merged, this.text);
    }

    private static Collection<Suggestion> mergeSuggestions(Collection<Suggestion> suggestions) {
        // Use a LinkedHashMap to keep the order
        return suggestions
            .stream()
            .collect(Collectors.toMap(Suggestion::getText, Function.identity(), Suggestion::merge, LinkedHashMap::new))
            .values();
    }

    private static List<Suggestion> sortSuggestions(Collection<Suggestion> suggestions, String originalText) {
        // Use a linked list to remove and rearrange easily
        List<Suggestion> sorted = new LinkedList<>();

        // If any of the suggestions matches the original then move it as the first suggestion
        // If not we add it
        boolean originalFound = false;
        for (Suggestion suggestion : suggestions) {
            if (suggestion.getText().equals(originalText)) {
                sorted.add(0, suggestion);
                originalFound = true;
            } else {
                sorted.add(suggestion);
            }
        }
        if (!originalFound) {
            sorted.add(0, Suggestion.ofNoComment(originalText));
        }

        return sorted;
    }
}
