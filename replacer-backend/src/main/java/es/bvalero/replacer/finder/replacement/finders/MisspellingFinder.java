package es.bvalero.replacer.finder.replacement.finders;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.MisspellingSuggestion;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.replacement.ReplacementSuggestion;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.SetValuedMap;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Abstract class for the common functionality of the misspelling finders.
 */
public abstract class MisspellingFinder implements ReplacementFinder {

    // Derived from the misspelling set to access faster by word
    private Map<WikipediaLanguage, Map<String, Misspelling>> misspellingMap = new EnumMap<>(WikipediaLanguage.class);

    private Map<String, Misspelling> getMisspellingMap(WikipediaLanguage lang) {
        return this.misspellingMap.get(lang);
    }

    @Loggable(value = Loggable.DEBUG, skipArgs = true, skipResult = true)
    void buildMisspellingMaps(SetValuedMap<WikipediaLanguage, Misspelling> misspellings) {
        // Build a map to quick access the misspellings by word
        Map<WikipediaLanguage, Map<String, Misspelling>> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : misspellings.keySet()) {
            map.put(lang, buildMisspellingMap(misspellings.get(lang)));
        }
        this.misspellingMap = map;
    }

    @VisibleForTesting
    public Map<String, Misspelling> buildMisspellingMap(Set<Misspelling> misspellings) {
        // Build a map to quick access the misspellings by word
        Map<String, Misspelling> map = new HashMap<>(misspellings.size());
        misspellings.forEach(
            misspelling -> {
                String word = misspelling.getWord();
                if (misspelling.isCaseSensitive()) {
                    map.put(word, misspelling);
                } else {
                    // If case-insensitive, we add to the map "word" and "Word".
                    map.put(FinderUtils.setFirstLowerCase(word), misspelling);
                    map.put(FinderUtils.setFirstUpperCase(word), misspelling);
                }
            }
        );
        return map;
    }

    boolean isExistingWord(String word, WikipediaLanguage lang) {
        return findMisspellingByWord(word, lang).isPresent();
    }

    @Override
    public Replacement convert(MatchResult matcher, FinderPage page) {
        int start = matcher.start();
        String text = matcher.group();
        return Replacement
            .builder()
            .type(getType())
            // .subtype(getSubtype(text, lang))
            .start(start)
            .text(text)
            //.suggestions(findSuggestions(text, lang))
            .build();
    }

    abstract ReplacementType getType();

    String getSubtype(String text, WikipediaLanguage lang) {
        // We are sure in this point that the Misspelling exists
        return findMisspellingByWord(text, lang).map(Misspelling::getWord).orElseThrow(IllegalArgumentException::new);
    }

    // Return the misspelling related to the given word, or empty if there is no such misspelling.
    public Optional<Misspelling> findMisspellingByWord(String word, WikipediaLanguage lang) {
        return Optional.ofNullable(getMisspellingMap(lang).get(word));
    }

    // Transform the case of the suggestion, e.g. "Habia" -> "Había"
    List<ReplacementSuggestion> findSuggestions(String originalWord, WikipediaLanguage lang) {
        // We are sure in this point that the Misspelling exists
        Misspelling misspelling = findMisspellingByWord(originalWord, lang).orElseThrow(IllegalArgumentException::new);
        return applyMisspellingSuggestions(originalWord, misspelling);
    }

    public static List<ReplacementSuggestion> applyMisspellingSuggestions(String word, Misspelling misspelling) {
        List<ReplacementSuggestion> suggestions = new LinkedList<>();
        for (MisspellingSuggestion misspellingSuggestion : misspelling.getSuggestions()) {
            ReplacementSuggestion suggestion = convertSuggestion(misspellingSuggestion);
            if (misspelling.isCaseSensitive()) {
                suggestions.add(suggestion);
            } else {
                // Apply the case of the original word to the generic misspelling suggestion
                suggestions.add(FinderUtils.startsWithUpperCase(word) ? suggestion.toUppercase() : suggestion);
            }
        }

        // Special case. For case-sensitive misspellings which transform uppercase to lowercase,
        // we also provide the uppercase version just in case it is correct according to punctuation rules.
        if (
            misspelling.isCaseSensitive() &&
            FinderUtils.startsWithUpperCase(word) &&
            !FinderUtils.isUppercase(word) &&
            suggestions.stream().map(ReplacementSuggestion::getText).noneMatch(word::equals)
        ) {
            suggestions
                .stream()
                .filter(s -> FinderUtils.toLowerCase(word).equals(s.getText()))
                .findAny()
                .ifPresent(s -> suggestions.add(s.toUppercase()));
        }

        // If any of the suggestions matches the original then move it as the first suggestion
        for (int i = 0; i < suggestions.size(); i++) {
            if (suggestions.get(i).getText().equals(word)) {
                ReplacementSuggestion original = suggestions.remove(i);
                suggestions.add(0, original);
                break;
            }
        }

        return suggestions;
    }

    private static ReplacementSuggestion convertSuggestion(MisspellingSuggestion misspellingSuggestion) {
        return ReplacementSuggestion.of(misspellingSuggestion.getText(), misspellingSuggestion.getComment());
    }
}
