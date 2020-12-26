package es.bvalero.replacer.finder.misspelling;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.SetValuedMap;

/**
 * Abstract class for the common functionality of the misspelling finders.
 */
public abstract class MisspellingFinder implements ReplacementFinder, PropertyChangeListener {
    // Derived from the misspelling set to access faster by word
    private Map<WikipediaLanguage, Map<String, Misspelling>> misspellingMap = new EnumMap<>(WikipediaLanguage.class);

    abstract MisspellingManager getMisspellingManager();

    @PostConstruct
    public void init() {
        getMisspellingManager().addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        SetValuedMap<WikipediaLanguage, Misspelling> misspellings = (SetValuedMap<WikipediaLanguage, Misspelling>) evt.getNewValue();
        this.misspellingMap = buildMisspellingMaps(misspellings);
        processMisspellingChange(misspellings);
    }

    @Loggable(value = Loggable.DEBUG, skipArgs = true, skipResult = true)
    private Map<WikipediaLanguage, Map<String, Misspelling>> buildMisspellingMaps(
        SetValuedMap<WikipediaLanguage, Misspelling> misspellings
    ) {
        // Build a map to quick access the misspellings by word
        Map<WikipediaLanguage, Map<String, Misspelling>> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : misspellings.keySet()) {
            map.put(lang, buildMisspellingMap(misspellings.get(lang)));
        }
        return map;
    }

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

    abstract void processMisspellingChange(SetValuedMap<WikipediaLanguage, Misspelling> misspellings);

    boolean isExistingWord(String word, WikipediaLanguage lang) {
        return findMisspellingByWord(word, lang).isPresent();
    }

    Replacement convertMatch(MatchResult matcher) {
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

    abstract String getType();

    String getSubtype(String text, WikipediaLanguage lang) {
        // We are sure in this point that the Misspelling exists
        return findMisspellingByWord(text, lang).map(Misspelling::getWord).orElseThrow(IllegalArgumentException::new);
    }

    // Return the misspelling related to the given word, or empty if there is no such misspelling.
    public Optional<Misspelling> findMisspellingByWord(String word, WikipediaLanguage lang) {
        return Optional.ofNullable(this.misspellingMap.getOrDefault(lang, Collections.emptyMap()).get(word));
    }

    // Transform the case of the suggestion, e.g. "Habia" -> "Había"
    List<Suggestion> findSuggestions(String originalWord, WikipediaLanguage lang) {
        List<Suggestion> suggestions = new LinkedList<>();

        // We are sure in this point that the Misspelling exists
        Misspelling misspelling = findMisspellingByWord(originalWord, lang).orElseThrow(IllegalArgumentException::new);
        misspelling
            .getSuggestions()
            .forEach(
                suggestion -> {
                    Suggestion newSuggestion = FinderUtils.startsWithUpperCase(originalWord) &&
                        !misspelling.isCaseSensitive()
                        ? Suggestion.of(FinderUtils.setFirstUpperCase(suggestion.getText()), suggestion.getComment())
                        : suggestion;

                    // If the suggested word matches the original then add it as the first suggestion
                    if (originalWord.equals(newSuggestion.getText())) {
                        suggestions.add(0, newSuggestion);
                    } else {
                        suggestions.add(newSuggestion);
                    }
                }
            );

        return suggestions;
    }
}
