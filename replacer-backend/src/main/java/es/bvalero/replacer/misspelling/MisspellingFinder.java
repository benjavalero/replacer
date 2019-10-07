package es.bvalero.replacer.misspelling;

import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.finder.Suggestion;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

// We make this implementation public to be used by the finder benchmarks
@Slf4j
public abstract class MisspellingFinder implements ReplacementFinder, PropertyChangeListener {

    // Derived from the misspelling set to access faster by word
    private Map<String, Misspelling> misspellingMap = new HashMap<>();

    abstract MisspellingManager getMisspellingManager();

    public Map<String, Misspelling> getMisspellingMap() {
        return misspellingMap;
    }

    @PostConstruct
    public void init() {
        getMisspellingManager().addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        Set<Misspelling> misspellings = (Set<Misspelling>) evt.getNewValue();
        this.misspellingMap = buildMisspellingMap(misspellings);
        processMisspellingChange(misspellings);
    }

    private Map<String, Misspelling> buildMisspellingMap(Set<Misspelling> misspellings) {
        LOGGER.info("START Build misspelling map");

        // Build a map to quick access the misspellings by word
        Map<String, Misspelling> map = new HashMap<>(misspellings.size());
        misspellings.forEach(misspelling -> {
            String word = misspelling.getWord();
            if (misspelling.isCaseSensitive()) {
                map.put(word, misspelling);
            } else {
                // If case-insensitive, we add to the map "word" and "Word".
                map.put(word, misspelling);
                map.put(FinderUtils.setFirstUpperCase(word), misspelling);
            }
        });

        LOGGER.info("END Build misspelling map. Size: {}", map.size());
        return map;
    }

    abstract void processMisspellingChange(Set<Misspelling> misspellings);

    @Override
    public List<Replacement> findReplacements(String text) {
        return findMatchResults(text, getAutomaton());
    }

    @Override
    public boolean isValidMatch(int start, String matchedText, String fullText) {
        return ReplacementFinder.super.isValidMatch(start, matchedText, fullText)
                && findMisspellingByWord(matchedText).isPresent();
    }

    abstract RunAutomaton getAutomaton();

    @Override
    public String getSubtype(String text) {
        // We are sure in this point that the Misspelling exists
        return findMisspellingByWord(text).map(Misspelling::getWord).orElseThrow(IllegalArgumentException::new);
    }

    /**
     * @return The misspelling related to the given word, or empty if there is no such misspelling.
     */
    private Optional<Misspelling> findMisspellingByWord(String word) {
        return Optional.ofNullable(this.misspellingMap.get(word));
    }

    /* Transform the case of the suggestion, e. g. "Habia" -> "Había" */
    @Override
    public List<Suggestion> findSuggestions(String originalWord) {
        List<Suggestion> suggestions = new LinkedList<>();

        // We are sure in this point that the Misspelling exists
        Misspelling misspelling = findMisspellingByWord(originalWord).orElseThrow(IllegalArgumentException::new);
        misspelling.getSuggestions().forEach(suggestion -> {
            Suggestion newSuggestion = FinderUtils.startsWithUpperCase(originalWord) && !misspelling.isCaseSensitive()
                    ? Suggestion.of(FinderUtils.setFirstUpperCase(suggestion.getText()), suggestion.getComment())
                    : suggestion;
            // If the suggested word matches the original then add it as the first suggestion
            if (originalWord.equals(newSuggestion.getText())) {
                suggestions.add(0, newSuggestion);
            } else {
                suggestions.add(newSuggestion);
            }
        });

        return suggestions;
    }

}
