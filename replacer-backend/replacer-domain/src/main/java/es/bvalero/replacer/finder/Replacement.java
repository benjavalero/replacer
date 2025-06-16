package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.util.ReplacerUtils;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

/**
 * A <strong>replacement</strong> is a potential issue to be checked and fixed (replaced). For instance,
 * the word "aproximated" is misspelled and therefore could be proposed to be replaced with "approximated".
 * Note the importance of the <em>potential</em> adjective, as an issue could be just a false positive.
 * For instance, in Spanish the word "Paris" could be misspelled if it corresponds to the French city
 * (written correctly as "París"), but it would be correct if it refers to the mythological Trojan prince.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Replacement implements FinderResult {

    private static final int CONTEXT_THRESHOLD = 20;
    private static final int MAX_CONTEXT_LENGTH = 255; // Constrained by the database

    @EqualsAndHashCode.Include
    private final int start;

    @EqualsAndHashCode.Include
    @NonNull
    private final String text;

    @NonNull
    private final ReplacementType type;

    @NonNull
    private final List<Suggestion> suggestions; // We use a list as the order is important

    @NonNull
    private final String context;

    private Replacement(
        int start,
        String text,
        ReplacementType type,
        Collection<Suggestion> suggestions,
        String pageContent
    ) {
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

        this.start = start;
        this.text = text;
        this.type = type;

        // Pre-calculate the suggestions and the context as they will always be used later
        this.suggestions = mergeSuggestions(suggestions);
        this.context = extractContext(pageContent);
    }

    public static Replacement of(
        int start,
        String text,
        ReplacementType type,
        Collection<Suggestion> suggestions,
        String pageContent
    ) {
        return new Replacement(start, text, type, suggestions, pageContent);
    }

    // Include the original text as the first option
    private List<Suggestion> mergeSuggestions(Collection<Suggestion> suggestions) {
        Collection<Suggestion> merged = Suggestion.mergeSuggestions(suggestions);
        return Suggestion.sortSuggestions(merged, this.text);
    }

    public static void removeNested(Collection<Replacement> results) {
        // Filter to return the results which are NOT strictly contained in any other
        results.removeIf(r -> results.stream().anyMatch(r2 -> r2.containsStrictly(r)));
    }

    private String extractContext(String pageContent) {
        return StringUtils.truncate(
            ReplacerUtils.getContextAroundWord(pageContent, start, getEnd(), CONTEXT_THRESHOLD),
            MAX_CONTEXT_LENGTH
        );
    }

    public Replacement withStart(int newStart) {
        return new Replacement(newStart, this.text, this.type, this.suggestions, this.context);
    }

    // We consider two replacements equal if they have the same start and end (or text).
    // For the sake of the tests, we will perform a deep comparison.
    @TestOnly
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
