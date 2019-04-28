package es.bvalero.replacer.misspelling;

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
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * Find misspelling replacements in a given text.
 * This algorithm is slower but uses less memory.
 */
@Component
@Profile("offline")
public class MisspellingSlowerFinder extends ReplacementFinder implements ArticleReplacementFinder, PropertyChangeListener {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingSlowerFinder.class);
    private static final RunAutomaton WORD_AUTOMATON = new RunAutomaton(new RegExp("(<L>|<N>)+")
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
    public void propertyChange(PropertyChangeEvent evt) {
        @SuppressWarnings("unchecked")
        Set<Misspelling> newMisspellings = (Set<Misspelling>) evt.getNewValue();
        buildMisspellingRelatedFields(newMisspellings);
    }

    void buildMisspellingRelatedFields(Set<Misspelling> newMisspellings) {
        this.misspellingMap = buildMisspellingMap(newMisspellings);
    }

    Map<String, Misspelling> buildMisspellingMap(Set<Misspelling> misspellings) {
        LOGGER.info("Start building misspelling map...");

        // Build a map to quick access the misspellings by word
        Map<String, Misspelling> misspellingMap = new HashMap<>(misspellings.size());
        for (Misspelling misspelling : misspellings) {
            if (misspelling.isCaseSensitive()) {
                misspellingMap.put(misspelling.getWord(), misspelling);
            } else {
                // If case-insensitive, we add to the map "word" and "Word".
                misspellingMap.put(misspelling.getWord(), misspelling);
                misspellingMap.put(setFirstUpperCase(misspelling.getWord()), misspelling);
            }
        }

        LOGGER.info("End building misspelling map");
        return misspellingMap;
    }

    /**
     * @return The given word turning the first letter into uppercase (if needed)
     */
    String setFirstUpperCase(String word) {
        return word.substring(0, 1).toUpperCase(Locale.forLanguageTag("es")) + word.substring(1);
    }

    /**
     * @return A list with the misspelling replacements in a given text.
     */
    @Override
    public List<ArticleReplacement> findReplacements(String text) {
        List<ArticleReplacement> articleReplacements = new ArrayList<>(100);

        // Find all the words and check if they are potential errors
        List<ArticleReplacement> textWords = findReplacements(text, WORD_AUTOMATON, ReplacementType.MISSPELLING);
        // For each word, check if it is a known potential misspelling.
        for (ArticleReplacement textWord : textWords) {
            String originalText = textWord.getText();
            Misspelling wordMisspelling = findMisspellingByWord(originalText);
            if (wordMisspelling != null) {
                articleReplacements.add(textWord
                        .withSubtype(wordMisspelling.getWord())
                        .withComment(wordMisspelling.getComment())
                        .withSuggestion(findMisspellingSuggestion(textWord.getText(), wordMisspelling)));
            }
        }

        return articleReplacements;
    }

    /**
     * @return The misspelling related to the given word, or null if there is no such misspelling.
     */
    Misspelling findMisspellingByWord(String word) {
        return this.misspellingMap.get(word);
    }

    String findMisspellingSuggestion(CharSequence originalWord, Misspelling misspelling) {
        List<String> suggestions = misspelling.getSuggestions();

        // TODO Take into account all the suggestions
        String suggestion = suggestions.get(0);

        if (startsWithUpperCase(originalWord) && !misspelling.isCaseSensitive()) {
            suggestion = setFirstUpperCase(suggestion);
        }

        return suggestion;
    }

}
