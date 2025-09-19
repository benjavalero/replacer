package es.bvalero.replacer.finder;

import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;

/**
 * A <strong>replacement</strong> is a potential issue to be checked and fixed (replaced). For instance,
 * the word "aproximated" is misspelled and therefore could be proposed to be replaced with "approximated".
 * Note the importance of the <em>potential</em> adjective, as an issue could be just a false positive.
 * For instance, in Spanish the word "Paris" could be misspelled if it corresponds to the French city
 * (written correctly as "París"), but it would be correct if it refers to the mythological Trojan prince.
 */
public record Replacement(int start, String text, ReplacementType type, List<Suggestion> suggestions)
    implements FinderResult {
    public Replacement(int start, String text, ReplacementType type, List<Suggestion> suggestions) {
        // Validate start
        if (start < 0) {
            throw new IllegalArgumentException("Negative replacement start: " + start);
        }

        // Validate text
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("Blank replacement text: " + start);
        }

        // There must exist at least a suggestion different from the found text
        if (!suggestions.isEmpty() && suggestions.stream().allMatch(s -> s.getText().equals(text))) {
            String msg = String.format("%s - %s", type, StringUtils.join(suggestions));
            throw new IllegalArgumentException("Invalid replacement suggestions: " + msg);
        }

        this.start = start;
        this.text = text;
        this.type = type;

        // Pre-calculate the final merged suggestions as they will always be used later
        this.suggestions = suggestions.isEmpty() ? suggestions : mergeSuggestions(suggestions);
    }

    public static Replacement of(int start, String text, ReplacementType type, List<Suggestion> suggestions) {
        if (suggestions.isEmpty()) {
            throw new IllegalArgumentException("Invalid replacement. Empty suggestions.");
        }

        return new Replacement(start, text, type, suggestions);
    }

    /* Particular case to avoid calculating suggestions when indexing to improve a little the performance */
    public static Replacement ofNoSuggestions(int start, String text, ReplacementType type) {
        return new Replacement(start, text, type, Collections.emptyList());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Replacement that)) return false;
        return start == that.start && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, text);
    }

    /* Remove the suggestions to improve performance */
    public Replacement withNoSuggestions() {
        return withSuggestions(Collections.emptyList());
    }

    private Replacement withSuggestions(List<Suggestion> suggestions) {
        return new Replacement(start, text, type, suggestions);
    }

    // Include the original text as the first option
    private List<Suggestion> mergeSuggestions(List<Suggestion> suggestions) {
        List<Suggestion> merged = Suggestion.mergeSuggestions(suggestions);
        return Suggestion.sortSuggestions(merged, this.text);
    }

    public static void removeNested(SortedSet<Replacement> results) {
        // Filter to return the results which are NOT strictly contained in any other
        results.removeIf(r -> results.stream().anyMatch(r2 -> r2.containsStrictly(r)));
    }

    public Replacement withStart(int newStart) {
        return new Replacement(newStart, this.text, this.type, this.suggestions);
    }

    // We consider two replacements equal if they have the same start and end (or text).
    // For the sake of the tests, we will perform a deep comparison.
    @TestOnly
    public static boolean compareReplacements(Collection<Replacement> expected, Collection<Replacement> actual) {
        if (expected.size() != actual.size()) {
            return false;
        }
        List<Replacement> expectedList = expected.stream().sorted(FinderResult::compareTo).toList();
        List<Replacement> actualList = actual.stream().sorted(FinderResult::compareTo).toList();
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
            expected.type.equals(actual.type) &&
            expected.suggestions().equals(actual.suggestions())
        );
    }
}
