package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.MisspellingManager;
import es.bvalero.replacer.finder.listing.Suggestion;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.SetValuedMap;

/**
 * Abstract class for the common functionality of the misspelling finders.
 */
abstract class MisspellingFinder implements ReplacementFinder, PropertyChangeListener {

    abstract MisspellingManager getMisspellingManager();

    @PostConstruct
    public void init() {
        getMisspellingManager().addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        if (MisspellingManager.PROPERTY_ITEMS.equals(evt.getPropertyName())) {
            SetValuedMap<WikipediaLanguage, Misspelling> misspellings = (SetValuedMap<WikipediaLanguage, Misspelling>) evt.getNewValue();
            processMisspellingChange(misspellings);
        }
    }

    abstract void processMisspellingChange(SetValuedMap<WikipediaLanguage, Misspelling> misspellings);

    boolean isExistingWord(String word, WikipediaLanguage lang) {
        return findMisspellingByWord(word, lang).isPresent();
    }

    @Override
    public Replacement convert(MatchResult matcher) {
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
        return Optional.ofNullable(getMisspellingManager().getMisspellingMap(lang).get(word));
    }

    // Transform the case of the suggestion, e.g. "Habia" -> "Había"
    List<Suggestion> findSuggestions(String originalWord, WikipediaLanguage lang) {
        // We are sure in this point that the Misspelling exists
        Misspelling misspelling = findMisspellingByWord(originalWord, lang).orElseThrow(IllegalArgumentException::new);
        return applyMisspellingSuggestions(originalWord, misspelling);
    }

    static List<Suggestion> applyMisspellingSuggestions(String word, Misspelling misspelling) {
        List<Suggestion> suggestions = new LinkedList<>();
        for (Suggestion misspellingSuggestion : misspelling.getSuggestions()) {
            if (misspelling.isCaseSensitive()) {
                suggestions.add(misspellingSuggestion);
            } else {
                // Apply the case of the original word to the generic misspelling suggestion
                suggestions.add(
                    FinderUtils.startsWithUpperCase(word) ? misspellingSuggestion.toUppercase() : misspellingSuggestion
                );
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
                .ifPresent(s -> suggestions.add(s.toUppercase()));
        }

        // If any of the suggestions matches the original then move it as the first suggestion
        for (int i = 0; i < suggestions.size(); i++) {
            if (suggestions.get(i).getText().equals(word)) {
                Suggestion original = suggestions.remove(i);
                suggestions.add(0, original);
                break;
            }
        }

        return suggestions;
    }
}
