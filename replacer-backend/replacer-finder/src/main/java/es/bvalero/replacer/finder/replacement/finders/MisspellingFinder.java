package es.bvalero.replacer.finder.replacement.finders;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.MatchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.lang3.StringUtils;

/**
 * Abstract class for the common functionality of the misspelling finders.
 */
@Slf4j
public abstract class MisspellingFinder implements ReplacementFinder {

    // Derived from the misspelling set to access faster by word
    // volatile: reference replaced atomically on reload; guarantees visibility across threads
    private volatile Map<WikipediaLanguage, Map<String, StandardMisspelling>> misspellingMap = new EnumMap<>(
        WikipediaLanguage.class
    );

    private Map<String, StandardMisspelling> getMisspellingMap(WikipediaLanguage lang) {
        final Map<String, StandardMisspelling> langMap = this.misspellingMap.get(lang);
        if (langMap == null) {
            LOGGER.warn("No misspelling map for lang {} (still loading)", lang);
            return Map.of();
        } else {
            return langMap;
        }
    }

    void buildMisspellingMaps(SetValuedMap<WikipediaLanguage, StandardMisspelling> misspellings) {
        // Build a map to quick access the misspellings by word
        final Map<WikipediaLanguage, Map<String, StandardMisspelling>> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : misspellings.keySet()) {
            map.put(lang, buildMisspellingMap(misspellings.get(lang)));
        }
        this.misspellingMap = map;
    }

    private Map<String, StandardMisspelling> buildMisspellingMap(Set<StandardMisspelling> misspellings) {
        // Build a map to quick access the misspellings by word
        final Map<String, StandardMisspelling> map = new HashMap<>(misspellings.size());
        misspellings.forEach(misspelling ->
            misspelling.getTerms().forEach(term -> map.put(term.intern(), misspelling))
        );
        return map;
    }

    boolean isExistingWord(String word, WikipediaLanguage lang) {
        return getMisspellingMap(lang).containsKey(word);
    }

    @Override
    public FinderPriority getPriority() {
        // All replacement finders have more priority than the misspelling ones
        // For instance if we detect "10º" as composed and as ordinal, we prefer the second one.
        return FinderPriority.NONE;
    }

    @Override
    public Replacement convertWithNoSuggestions(MatchResult matcher, FinderPage page) {
        final int start = matcher.start();
        final String text = matcher.group();
        return Replacement.ofNoSuggestions(
            start,
            text,
            StandardType.of(getType(), getSubtype(text, page.getPageKey().getLang()))
        );
    }

    @Override
    public Replacement convert(MatchResult matcher, FinderPage page) {
        final int start = matcher.start();
        final String text = matcher.group();
        return Replacement.of(
            start,
            text,
            StandardType.of(getType(), getSubtype(text, page.getPageKey().getLang())),
            findSuggestions(text, page.getPageKey().getLang())
        );
    }

    protected abstract ReplacementKind getType();

    private String getSubtype(String text, WikipediaLanguage lang) {
        // We are sure in this point that the Misspelling exists
        return findMisspellingByWord(text, lang)
            .map(StandardMisspelling::getWord)
            .orElseThrow(IllegalArgumentException::new);
    }

    // Return the misspelling related to the given word, or empty if there is no such misspelling.
    private Optional<StandardMisspelling> findMisspellingByWord(String word, WikipediaLanguage lang) {
        return Optional.ofNullable(getMisspellingMap(lang).get(word));
    }

    // Transform the case of the suggestion, e.g. "Habia" -> "Había"
    // If a suggestion is itself a known misspelling (chained), its suggestions are also added (one level only).
    // Duplicates will be merged later by Suggestion.mergeSuggestions() in the Replacement constructor.
    // NOTE: Only one level of chaining is supported. If in the future a case requiring deeper propagation
    // appears, this method could be made recursive with a visited set to avoid infinite loops.
    private List<Suggestion> findSuggestions(String originalWord, WikipediaLanguage lang) {
        // We are sure in this point that the Misspelling exists
        final StandardMisspelling misspelling = findMisspellingByWord(originalWord, lang).orElseThrow(
            IllegalArgumentException::new
        );
        final List<Suggestion> directSuggestions = applyMisspellingSuggestions(originalWord, misspelling);

        final List<Suggestion> resolved = new ArrayList<>();
        for (Suggestion s : directSuggestions) {
            resolved.add(s);
            if (!s.getText().equals(originalWord)) {
                final Optional<StandardMisspelling> chained = findMisspellingByWord(s.getText(), lang);
                if (chained.isPresent() && !chained.get().equals(misspelling)) {
                    // The suggestion is itself a different misspelling: add its suggestions too (one level)
                    resolved.addAll(applyMisspellingSuggestions(s.getText(), chained.get()));
                }
            }
        }
        return resolved;
    }

    protected List<Suggestion> applyMisspellingSuggestions(String word, Misspelling misspelling) {
        final List<Suggestion> suggestions = new ArrayList<>();
        for (MisspellingSuggestion misspellingSuggestion : misspelling.getSuggestions()) {
            final Suggestion suggestion = convertSuggestion(misspellingSuggestion);
            if (misspelling.isCaseSensitive()) {
                suggestions.add(suggestion);
            } else {
                // Apply the case of the original word to the generic misspelling suggestion
                suggestions.add(FinderUtils.startsWithUpperCase(word) ? suggestion.toUpperCase() : suggestion);
            }
        }

        // Special case. For case-sensitive misspellings which transform uppercase to lowercase,
        // we also provide the uppercase version just in case it is correct according to punctuation rules.
        if (
            misspelling.isCaseSensitive() &&
            FinderUtils.startsWithUpperCase(word) &&
            !StringUtils.isAllUpperCase(word) &&
            suggestions.stream().map(Suggestion::getText).noneMatch(word::equals)
        ) {
            suggestions
                .stream()
                .filter(s -> ReplacerUtils.toLowerCase(word).equals(s.getText()))
                .findAny()
                .ifPresent(s -> suggestions.add(s.toUpperCase()));
        }

        return suggestions;
    }

    private static Suggestion convertSuggestion(MisspellingSuggestion misspellingSuggestion) {
        return Suggestion.of(misspellingSuggestion.getText(), misspellingSuggestion.getComment());
    }

    @Override
    public Optional<StandardType> findMatchingReplacementType(
        WikipediaLanguage lang,
        String replacement,
        boolean caseSensitive
    ) {
        final String word =
            !caseSensitive && FinderUtils.startsWithUpperCase(replacement)
                ? ReplacerUtils.toLowerCase(replacement)
                : replacement;
        Optional<StandardMisspelling> misspelling = findMisspellingByWord(word, lang);
        StandardType type = null;
        if (
            misspelling.isPresent() &&
            ((misspelling.get().isCaseSensitive() && caseSensitive) ||
                (!misspelling.get().isCaseSensitive() && !caseSensitive))
        ) {
            type = StandardType.of(misspelling.get().getReplacementKind(), misspelling.get().getWord());
        }

        return Optional.ofNullable(type);
    }
}
