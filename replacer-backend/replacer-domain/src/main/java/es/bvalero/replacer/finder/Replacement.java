package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.util.ReplacerUtils;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

/**
 * A <strong>replacement</strong> is a potential issue to be checked and fixed (replaced). For instance,
 * the word "aproximated" is misspelled and therefore could be proposed to be replaced with "approximated".
 * Note the importance of the <em>potential</em> adjective, as an issue could be just a false positive.
 * For instance, in Spanish the word "Paris" could be misspelled if it corresponds to the French city
 * (written correctly as "París"), but it would be correct if it refers to the mythological Trojan prince.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Builder
public class Replacement implements FinderResult {

    private static final int CONTEXT_THRESHOLD = 20;
    private static final int MAX_CONTEXT_LENGTH = 255; // Constrained by the database

    // TODO: To be removed once the replacements always go along with a page as an aggregate
    @NonNull
    FinderPage page;

    @EqualsAndHashCode.Include
    @With
    int start;

    @EqualsAndHashCode.Include
    @NonNull
    String text;

    @NonNull
    ReplacementType type;

    @NonNull
    Collection<Suggestion> suggestions;

    private Replacement(
        FinderPage page,
        int start,
        String text,
        ReplacementType type,
        Collection<Suggestion> suggestions
    ) {
        // Implement the private constructor to perform validations when building by Lombok

        // Validate start
        if (start < 0) {
            throw new IllegalArgumentException("Negative replacement start: " + start);
        }

        // Validate text
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("Blank replacement text: " + start);
        }

        // There must exist at least a suggestion different from the found text
        if (suggestions.isEmpty() || suggestions.stream().allMatch(s -> s.getText().equals(text))) {
            String msg = String.format("%s - %s", type, StringUtils.join(suggestions));
            throw new IllegalArgumentException("Invalid replacement suggestions: " + msg);
        }

        this.page = page;
        this.start = start;
        this.text = text;
        this.type = type;
        this.suggestions = suggestions;
    }

    // Overwrite the getter to include the original text as the first option
    public List<Suggestion> getSuggestions() {
        Collection<Suggestion> merged = Suggestion.mergeSuggestions(this.suggestions);
        return Suggestion.sortSuggestions(merged, this.text);
    }

    public static void removeNested(Collection<Replacement> results) {
        // Filter to return the results which are NOT strictly contained in any other
        results.removeIf(r -> results.stream().anyMatch(r2 -> r2.containsStrictly(r)));
    }

    public String getContext() {
        return StringUtils.truncate(
            ReplacerUtils.getContextAroundWord(page.getContent(), start, getEnd(), CONTEXT_THRESHOLD),
            MAX_CONTEXT_LENGTH
        );
    }

    // We consider two replacements equal if they have the same start and end (or text).
    // For the sake of the tests, we will perform a deep comparison.
    public static boolean compareReplacements(Collection<Replacement> expected, Collection<Replacement> actual) {
        if (expected.size() != actual.size()) {
            return false;
        }
        List<Replacement> expectedList = expected
            .stream()
            .sorted(Comparator.comparingInt(Replacement::getStart))
            .collect(Collectors.toCollection(LinkedList::new));
        List<Replacement> actualList = actual
            .stream()
            .sorted(Comparator.comparingInt(Replacement::getStart))
            .collect(Collectors.toCollection(LinkedList::new));
        for (int i = 0; i < expected.size(); i++) {
            if (!compareReplacement(expectedList.get(i), actualList.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean compareReplacement(Replacement expected, Replacement actual) {
        return (
            expected.equals(actual) &&
            expected.getType().equals(actual.getType()) &&
            expected.getSuggestions().equals(actual.getSuggestions())
        );
    }
}
