package es.bvalero.replacer.misspelling;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaFacade;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Stream;

/**
 * Manages the cache for the misspelling list in order to reduce the calls to Wikipedia.
 */
@Service
public class MisspellingManager {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingManager.class);
    private static final String CASE_SENSITIVE_VALUE = "cs";
    private static final int MISSPELLING_ESTIMATED_COUNT = 20000;
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_UPPERCASE_WORDS = "[\\\\.!*#|=]<Z>?(%s)";

    // Derived from the misspelling list to access faster by word
    private final Map<String, Misspelling> misspellings = new HashMap<>(MISSPELLING_ESTIMATED_COUNT);

    @Autowired
    private IWikipediaFacade wikipediaFacade;

    // Regex with all the misspellings
    // IMPORTANT : WE NEED AT LEAST 2 MB OF STACK SIZE -Xss2m !!!
    private RunAutomaton misspellingAutomaton;

    // Regex with the misspellings which start with uppercase and are case-sensitive
    // and starting with a special character which justifies the uppercase
    private RunAutomaton uppercaseAutomaton;

    static Collection<Misspelling> parseMisspellingListText(String misspellingListText) {
        Collection<Misspelling> misspellingSet = new HashSet<>(MISSPELLING_ESTIMATED_COUNT);

        Stream<String> stream = new BufferedReader(new StringReader(misspellingListText)).lines();
        stream.forEach(strLine -> {
            Misspelling misspelling = parseMisspellingLine(strLine);
            // Add the misspelling and check if it is duplicated
            if (misspelling != null && !misspellingSet.add(misspelling)) {
                LOGGER.warn("Duplicated misspelling term: {}", misspelling.getWord());
            }
        });

        return misspellingSet;
    }

    private static @Nullable Misspelling parseMisspellingLine(String misspellingLine) {
        Misspelling misspelling = null;

        String[] tokens = misspellingLine.split("\\|");
        // Ignore the lines not corresponding to misspelling lines
        if (!misspellingLine.isEmpty() && misspellingLine.startsWith(" ")) {
            if (tokens.length == 3) {
                misspelling = Misspelling.builder()
                        .setWord(tokens[0].trim())
                        .setCaseSensitive(CASE_SENSITIVE_VALUE.equalsIgnoreCase(tokens[1].trim()))
                        .setComment(tokens[2].trim())
                        .build();
            } else {
                LOGGER.warn("Bad formatted misspelling line: {}", misspellingLine);
            }
        }

        return misspelling;
    }

    /**
     * @return The given word turning the first letter into uppercase (if needed)
     */
    static String setFirstUpperCase(String word) {
        return word.substring(0, 1).toUpperCase(Locale.forLanguageTag("es")) + word.substring(1);
    }

    /**
     * @return If the first letter of the word is uppercase
     */
    static boolean startsWithUpperCase(CharSequence word) {
        return Character.isUpperCase(word.charAt(0));
    }

    static Map<String, Misspelling> buildMisspellingMap(Collection<Misspelling> misspellingList) {
        // Build a map to quick access the misspellings by word
        Map<String, Misspelling> misspellingMap = new HashMap<>(misspellingList.size());
        for (Misspelling misspelling : misspellingList) {
            if (misspelling.isCaseSensitive()) {
                misspellingMap.put(misspelling.getWord(), misspelling);
            } else {
                // If case-insensitive, we add to the map "word" and "Word".
                misspellingMap.put(misspelling.getWord(), misspelling);
                misspellingMap.put(setFirstUpperCase(misspelling.getWord()), misspelling);
            }
        }
        return misspellingMap;
    }

    static RunAutomaton buildMisspellingAutomaton(Collection<Misspelling> misspellingList) {
        LOGGER.info("Start building misspelling automaton...");

        // Build a long long regex with all the misspellings
        Collection<String> alternations = new ArrayList<>(misspellingList.size());
        for (Misspelling misspelling : misspellingList) {
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

    static RunAutomaton buildUppercaseAutomaton(Collection<Misspelling> misspellingList) {
        LOGGER.info("Start building uppercase automaton...");

        // Build an automaton with the misspellings starting with uppercase
        Collection<String> alternations = new ArrayList<>(misspellingList.size());
        for (Misspelling misspelling : misspellingList) {
            if (misspelling.isCaseSensitive() && startsWithUpperCase(misspelling.getWord())) {
                alternations.add(misspelling.getWord());
            }
        }
        String regexAlternations = String.format(REGEX_UPPERCASE_WORDS, StringUtils.join(alternations, "|"));
        RunAutomaton automaton = new RunAutomaton(new RegExp(regexAlternations).toAutomaton(new DatatypesAutomatonProvider()));

        LOGGER.info("End building uppercase automaton");

        return automaton;
    }

    synchronized Map<String, Misspelling> getMisspellings() {
        if (misspellings.isEmpty()) { // For the first time
            updateMisspellings();
        }
        return Collections.unmodifiableMap(misspellings);
    }

    @TestOnly
    synchronized void setMisspellings(Map<String, Misspelling> misspellings) {
        this.misspellings.clear();
        this.misspellings.putAll(misspellings);
    }

    synchronized RunAutomaton getMisspellingAutomaton() {
        if (misspellingAutomaton == null) { // For the first time
            updateMisspellings();
        }
        return misspellingAutomaton;
    }

    private synchronized void setMisspellingAutomaton(RunAutomaton misspellingAutomaton) {
        this.misspellingAutomaton = misspellingAutomaton;
    }

    public synchronized RunAutomaton getUppercaseAutomaton() {
        if (uppercaseAutomaton == null) { // For the first time
            updateMisspellings();
        }
        return uppercaseAutomaton;
    }

    private synchronized void setUppercaseAutomaton(RunAutomaton uppercaseAutomaton) {
        this.uppercaseAutomaton = uppercaseAutomaton;
    }

    /**
     * Update daily the list of misspellings from Wikipedia.
     */
/*
    @SuppressWarnings("WeakerAccess")
    */
    @SuppressWarnings("CallToSimpleSetterFromWithinClass")
    @Scheduled(fixedDelay = 3600 * 24 * 1000, initialDelay = 3600 * 24 * 1000)
    void updateMisspellings() {
        LOGGER.info("Start loading misspelling list from Wikipedia...");
        Collection<Misspelling> newMisspellingList = findWikipediaMisspellings();
        LOGGER.info("End parsing misspelling list from Wikipedia: {} items", newMisspellingList.size());

        if (!newMisspellingList.isEmpty()) {
            misspellings.clear();
            misspellings.putAll(buildMisspellingMap(newMisspellingList));

            // "Empty" the existing automaton to try to free some memory before building the new one
            setMisspellingAutomaton(null);
            setMisspellingAutomaton(buildMisspellingAutomaton(newMisspellingList));

            setUppercaseAutomaton(null);
            setUppercaseAutomaton(buildUppercaseAutomaton(newMisspellingList));
        }
    }

    Collection<Misspelling> findWikipediaMisspellings() {
        try {
            String misspellingListText = wikipediaFacade.getArticleContent(WikipediaFacade.MISSPELLING_LIST_ARTICLE);
            return parseMisspellingListText(misspellingListText);
        } catch (WikipediaException e) {
            LOGGER.error("Error loading misspellings list from Wikipedia");
            return Collections.emptyList();
        }
    }

    /**
     * @return The misspelling related to the given word, or null if there is no such misspelling.
     */
    Misspelling findMisspellingByWord(String word) {
        return getMisspellings().get(word);
    }

}
