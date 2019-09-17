package es.bvalero.replacer.misspelling;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ArticleReplacementFinder;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.finder.ReplacementSuggestion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * Find misspelling replacements in a given text.
 * Based in the WordAutomatonAllFinder winner in the benchmarks.
 */
@Slf4j
@Component
public class MisspellingFinder extends ReplacementFinder implements ArticleReplacementFinder, PropertyChangeListener {

    private static final String TYPE_MISSPELLING = "Ortografía";
    private static final RunAutomaton AUTOMATON_WORD = new RunAutomaton(new RegExp("(<L>|[-'])+")
            .toAutomaton(new DatatypesAutomatonProvider()));

    @Autowired
    private MisspellingManager misspellingManager;

    // Derived from the misspelling set to access faster by word
    private Map<String, Misspelling> misspellingMap = new HashMap<>();

    public Map<String, Misspelling> getMisspellingMap() {
        return misspellingMap;
    }

    @PostConstruct
    public void init() {
        misspellingManager.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        this.misspellingMap = buildMisspellingMap((Set<Misspelling>) evt.getNewValue());
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
                map.put(setFirstUpperCase(word), misspelling);
            }
        });

        LOGGER.info("END Build misspelling map. Size: {}", map.size());
        return map;
    }

    /**
     * @return A list with the misspelling replacements in a given text.
     */
    @Override
    public List<ArticleReplacement> findReplacements(String text) {
        List<ArticleReplacement> articleReplacements = new ArrayList<>(100);

        // Find all the words and check if they are potential errors
        findMatchResults(text, getAutomaton()).stream()
                .filter(match -> isWordCompleteInText(match.getStart(), match.getText(), text))
                .forEach(match -> findMisspellingByWord(match.getText()).ifPresent(misspelling ->
                        articleReplacements.add(convertMatchResultToReplacement(
                                match,
                                TYPE_MISSPELLING,
                                misspelling.getWord(),
                                findMisspellingSuggestions(match.getText(), misspelling)
                        ))));

        return articleReplacements;
    }

    RunAutomaton getAutomaton() {
        return AUTOMATON_WORD;
    }

    /**
     * @return The misspelling related to the given word, or empty if there is no such misspelling.
     */
    private Optional<Misspelling> findMisspellingByWord(String word) {
        return Optional.ofNullable(this.misspellingMap.get(word));
    }

    /* Transform the case of the suggestion, e. g. "Habia" -> "Había" */
    private List<ReplacementSuggestion> findMisspellingSuggestions(CharSequence originalWord, Misspelling misspelling) {
        List<ReplacementSuggestion> suggestions = new ArrayList<>();

        misspelling.getSuggestions().forEach(suggestion -> {
            ReplacementSuggestion newSuggestion = startsWithUpperCase(originalWord) && !misspelling.isCaseSensitive()
                    ? ReplacementSuggestion.of(setFirstUpperCase(suggestion.getText()), suggestion.getComment())
                    : suggestion;
            if (originalWord.equals(newSuggestion.getText())) {
                suggestions.add(0, newSuggestion);
            } else {
                suggestions.add(newSuggestion);
            }
        });

        return suggestions;
    }

}
