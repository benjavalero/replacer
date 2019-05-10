package es.bvalero.replacer.misspelling;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ArticleReplacementFinder;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Component
public class MisspellingFinder extends ReplacementFinder implements ArticleReplacementFinder, PropertyChangeListener {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingFinder.class);
    private static final RunAutomaton WORD_AUTOMATON = new RunAutomaton(new RegExp("(<L>|[-'])+")
            .toAutomaton(new DatatypesAutomatonProvider()));

    @Autowired
    private MisspellingManager misspellingManager;

    // Derived from the misspelling set to access faster by word
    private Map<String, Misspelling> misspellingMap = new HashMap<>();

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
        LOGGER.info("Start building misspelling map...");

        // Build a map to quick access the misspellings by word
        Map<String, Misspelling> misspellingMap = new HashMap<>(misspellings.size());
        misspellings.forEach(misspelling -> {
            String word = misspelling.getWord();
            if (misspelling.isCaseSensitive()) {
                misspellingMap.put(word, misspelling);
            } else {
                // If case-insensitive, we add to the map "word" and "Word".
                misspellingMap.put(word, misspelling);
                misspellingMap.put(setFirstUpperCase(word), misspelling);
            }
        });

        LOGGER.info("End building misspelling map");
        return misspellingMap;
    }

    private String setFirstUpperCase(String word) {
        return word.substring(0, 1).toUpperCase(Locale.forLanguageTag("es")) + word.substring(1);
    }

    /**
     * @return A list with the misspelling replacements in a given text.
     */
    @Override
    public List<ArticleReplacement> findReplacements(String text) {
        List<ArticleReplacement> articleReplacements = new ArrayList<>(100);

        // Find all the words and check if they are potential errors
        AutomatonMatcher m = WORD_AUTOMATON.newMatcher(text);
        while (m.find()) {
            String word = m.group();
            Misspelling misspelling = findMisspellingByWord(word);
            if (misspelling != null) {
                articleReplacements.add(ArticleReplacement.builder()
                        .setStart(m.start())
                        .setText(word)
                        .setType(ReplacementType.MISSPELLING)
                        .setSubtype(misspelling.getWord())
                        .setComment(misspelling.getComment())
                        .setSuggestion(findMisspellingSuggestion(word, misspelling))
                        .build());
            }
        }

        return articleReplacements;
    }

    /**
     * @return The misspelling related to the given word, or null if there is no such misspelling.
     */
    private Misspelling findMisspellingByWord(String word) {
        return this.misspellingMap.get(word);
    }

    /* Transform the case of the suggestion, e. g. "Habia" -> "Había" */
    private String findMisspellingSuggestion(CharSequence originalWord, Misspelling misspelling) {
        List<String> suggestions = misspelling.getSuggestions();

        // TODO Take into account all the suggestions
        String suggestion = suggestions.get(0);

        if (startsWithUpperCase(originalWord) && !misspelling.isCaseSensitive()) {
            suggestion = setFirstUpperCase(suggestion);
        }

        return suggestion;
    }

}
