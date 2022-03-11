package es.bvalero.replacer.finder.replacement.finders;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.Suggestion;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.MisspellingSuggestion;
import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.MatchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.boot.logging.LogLevel;

/**
 * Abstract class for the common functionality of the misspelling finders.
 */
@Slf4j
public abstract class MisspellingFinder implements ReplacementFinder {

    // Derived from the misspelling set to access faster by word
    private Map<WikipediaLanguage, Map<String, Misspelling>> misspellingMap = new EnumMap<>(WikipediaLanguage.class);

    private Map<String, Misspelling> getMisspellingMap(WikipediaLanguage lang) {
        final Map<String, Misspelling> langMap = this.misspellingMap.get(lang);
        if (langMap == null) {
            LOGGER.error("No misspelling map for lang {}", lang);
            return Collections.emptyMap();
        } else {
            return langMap;
        }
    }

    @Loggable(value = LogLevel.DEBUG, skipArgs = true, skipResult = true)
    void buildMisspellingMaps(SetValuedMap<WikipediaLanguage, Misspelling> misspellings) {
        // Build a map to quick access the misspellings by word
        final Map<WikipediaLanguage, Map<String, Misspelling>> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : misspellings.keySet()) {
            map.put(lang, buildMisspellingMap(misspellings.get(lang)));
        }
        this.misspellingMap = map;
    }

    @VisibleForTesting
    public Map<String, Misspelling> buildMisspellingMap(Set<Misspelling> misspellings) {
        // Build a map to quick access the misspellings by word
        final Map<String, Misspelling> map = new HashMap<>(misspellings.size());
        misspellings.forEach(misspelling -> {
            final String word = misspelling.getWord();
            if (misspelling.isCaseSensitive()) {
                map.put(word, misspelling);
            } else {
                // If case-insensitive, we add to the map "word" and "Word".
                map.put(FinderUtils.setFirstLowerCase(word), misspelling);
                map.put(FinderUtils.setFirstUpperCase(word), misspelling);
            }
        });
        return map;
    }

    @Override
    public boolean validate(MatchResult match, WikipediaPage page) {
        return isExistingWord(match.group(), page.getId().getLang()) && ReplacementFinder.super.validate(match, page);
    }

    private boolean isExistingWord(String word, WikipediaLanguage lang) {
        return getMisspellingMap(lang).containsKey(word);
    }

    @Override
    public Replacement convert(MatchResult matcher, WikipediaPage page) {
        final int start = matcher.start();
        final String text = matcher.group();
        return Replacement
            .builder()
            .type(ReplacementType.of(getType(), getSubtype(text, page.getId().getLang())))
            .start(start)
            .text(text)
            .suggestions(findSuggestions(text, page.getId().getLang()))
            .build();
    }

    abstract ReplacementKind getType();

    String getSubtype(String text, WikipediaLanguage lang) {
        // We are sure in this point that the Misspelling exists
        return findMisspellingByWord(text, lang).map(Misspelling::getWord).orElseThrow(IllegalArgumentException::new);
    }

    // Return the misspelling related to the given word, or empty if there is no such misspelling.
    private Optional<Misspelling> findMisspellingByWord(String word, WikipediaLanguage lang) {
        return Optional.ofNullable(getMisspellingMap(lang).get(word));
    }

    // Transform the case of the suggestion, e.g. "Habia" -> "Había"
    List<Suggestion> findSuggestions(String originalWord, WikipediaLanguage lang) {
        // We are sure in this point that the Misspelling exists
        final Misspelling misspelling = findMisspellingByWord(originalWord, lang)
            .orElseThrow(IllegalArgumentException::new);
        return applyMisspellingSuggestions(originalWord, misspelling);
    }

    public static List<Suggestion> applyMisspellingSuggestions(String word, Misspelling misspelling) {
        final List<Suggestion> suggestions = new ArrayList<>();
        for (MisspellingSuggestion misspellingSuggestion : misspelling.getSuggestions()) {
            final Suggestion suggestion = convertSuggestion(misspellingSuggestion);
            if (misspelling.isCaseSensitive()) {
                suggestions.add(suggestion);
            } else {
                // Apply the case of the original word to the generic misspelling suggestion
                suggestions.add(FinderUtils.startsWithUpperCase(word) ? toUppercase(suggestion) : suggestion);
            }
        }

        // Special case. For case-sensitive misspellings which transform uppercase to lowercase,
        // we also provide the uppercase version just in case it is correct according to punctuation rules.
        if (
            misspelling.isCaseSensitive() &&
            FinderUtils.startsWithUpperCase(word) &&
            !FinderUtils.isUppercase(word) &&
            suggestions.stream().map(Suggestion::getText).noneMatch(word::equals)
        ) {
            suggestions
                .stream()
                .filter(s -> FinderUtils.toLowerCase(word).equals(s.getText()))
                .findAny()
                .ifPresent(s -> suggestions.add(toUppercase(s)));
        }

        return suggestions;
    }

    private static Suggestion convertSuggestion(MisspellingSuggestion misspellingSuggestion) {
        return Suggestion.of(misspellingSuggestion.getText(), misspellingSuggestion.getComment());
    }

    static Suggestion toUppercase(Suggestion suggestion) {
        return Suggestion.of(FinderUtils.setFirstUpperCase(suggestion.getText()), suggestion.getComment());
    }

    @Override
    public Optional<ReplacementType> findMatchingReplacementType(
        WikipediaLanguage lang,
        String replacement,
        boolean caseSensitive
    ) {
        final String word = !caseSensitive && FinderUtils.startsWithUpperCase(replacement)
            ? FinderUtils.toLowerCase(replacement)
            : replacement;
        Optional<Misspelling> misspelling = findMisspellingByWord(word, lang);
        ReplacementType type = null;
        if (
            misspelling.isPresent() &&
            (
                (misspelling.get().isCaseSensitive() && caseSensitive) ||
                (!misspelling.get().isCaseSensitive() && !caseSensitive)
            )
        ) {
            type = ReplacementType.of(misspelling.get().getReplacementKind(), misspelling.get().getWord());
        }

        return Optional.ofNullable(type);
    }
}
