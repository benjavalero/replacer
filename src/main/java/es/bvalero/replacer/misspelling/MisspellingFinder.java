package es.bvalero.replacer.misspelling;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

/**
 * Find misspelling replacements in a given text.
 */
@Component
@Profile("default")
public class MisspellingFinder implements ArticleReplacementFinder, PropertyChangeListener {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingFinder.class);
    private static final Pattern PATTERN_BRACKETS = Pattern.compile("\\(.+?\\)");

    @Autowired
    private MisspellingManager misspellingManager;

    // Regex with all the misspellings
    // IMPORTANT : WE NEED AT LEAST 2 MB OF STACK SIZE -Xss2m !!!
    private RunAutomaton misspellingAutomaton;

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
        this.misspellingAutomaton = buildMisspellingAutomaton(newMisspellings);
        this.misspellingMap = buildMisspellingMap(newMisspellings);
    }

    private RunAutomaton buildMisspellingAutomaton(Set<Misspelling> misspellings) {
        LOGGER.info("Start building misspelling automaton...");

        // Build a long long regex with all the misspellings
        List<String> alternations = new ArrayList<>(misspellings.size());
        for (Misspelling misspelling : misspellings) {
            // If the misspelling contains a dot we escape it
            String word = misspelling.getWord()
                    .replace(".", "\\.");

            if (misspelling.isCaseSensitive()) {
                alternations.add(word);
            } else {
                // If case-insensitive, we add to the map "[wW]ord".
                String firstLetter = word.substring(0, 1);
                String newWord = '[' + firstLetter + firstLetter.toUpperCase(Locale.forLanguageTag("es")) + ']' + word.substring(1);
                alternations.add(newWord);
            }
        }
        String regexAlternations = '(' + StringUtils.join(alternations, "|") + ')';
        RunAutomaton automaton = new RunAutomaton(new RegExp(regexAlternations).toAutomaton());

        LOGGER.info("End building misspelling automaton");
        return automaton;
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

        List<ArticleReplacement> misspellingMatches = ArticleReplacementFinder.findReplacements(text,
                this.misspellingAutomaton, ReplacementType.MISSPELLING);

        for (ArticleReplacement misspellingMatch : misspellingMatches) {
            // The regex may find misspellings which are not complete words, e. g. "és" inside "inglés"
            //noinspection OverlyComplexBooleanExpression
            if (misspellingMatch.getStart() == 0 || misspellingMatch.getEnd() == text.length() ||
                    (!Character.isLetterOrDigit(text.charAt(misspellingMatch.getStart() - 1))
                            && !Character.isLetterOrDigit(text.charAt(misspellingMatch.getEnd())))) {
                Misspelling wordMisspelling = findMisspellingByWord(misspellingMatch.getText());

                articleReplacements.add(misspellingMatch
                        .withSubtype(wordMisspelling.getWord())
                        .withComment(wordMisspelling.getComment())
                        .withSuggestion(findMisspellingSuggestion(misspellingMatch.getText(), wordMisspelling)));
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
        List<String> suggestions = parseCommentSuggestions(misspelling);

        // TODO Take into account all the suggestions
        String suggestion = suggestions.get(0);

        if (MisspellingManager.startsWithUpperCase(originalWord) && !misspelling.isCaseSensitive()) {
            suggestion = setFirstUpperCase(suggestion);
        }

        return suggestion;
    }

    List<String> parseCommentSuggestions(Misspelling misspelling) {
        List<String> suggestions = new ArrayList<>(5);

        String suggestionNoBrackets = PATTERN_BRACKETS.matcher(misspelling.getComment()).replaceAll("");
        for (String suggestion : suggestionNoBrackets.split(",")) {
            String suggestionWord = suggestion.trim();

            // Don't suggest the misspelling main word
            if (StringUtils.isNotBlank(suggestionWord) && !suggestionWord.equals(misspelling.getWord())) {
                suggestions.add(suggestionWord);
            }
        }

        return suggestions;
    }

}
