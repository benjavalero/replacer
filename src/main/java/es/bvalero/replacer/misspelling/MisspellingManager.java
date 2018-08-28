package es.bvalero.replacer.misspelling;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaFacade;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Manages the cache for the misspelling list in order to reduce the calls to Wikipedia.
 */
@Service
public class MisspellingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingManager.class);
    // Derived from the misspelling list to access faster by word
    private final Map<String, Misspelling> misspellingMap = new HashMap<>(20000);
    @Autowired
    private IWikipediaFacade wikipediaFacade;
    // Regex with the alternations of all the misspellings
    // IMPORTANT : WE NEED AT LEAST 2 MB OF STACK SIZE -Xss2m !!!
    private RunAutomaton misspellingAlternationsAutomaton = null;
    private RunAutomaton uppercaseMisspellingsAutomaton = null;

    @NotNull
    Map<String, Misspelling> getMisspellingMap() {
        if (this.misspellingMap.isEmpty()) { // For the first time
            updateMisspellings();
        }
        return this.misspellingMap;
    }

    @NotNull
    public RunAutomaton getMisspellingAlternationsAutomaton() {
        if (this.misspellingAlternationsAutomaton == null) { // For the first time
            updateMisspellings();
        }
        return this.misspellingAlternationsAutomaton;
    }

    @NotNull
    public RunAutomaton getUppercaseMisspellingsAutomaton() {
        if (this.uppercaseMisspellingsAutomaton == null) { // For the first time
            updateMisspellings();
        }
        return this.uppercaseMisspellingsAutomaton;
    }

    /**
     * Update daily the list of misspellings from Wikipedia.
     */
    @SuppressWarnings("WeakerAccess")
    @Scheduled(fixedDelay = 3600 * 24 * 1000, initialDelay = 3600 * 24 * 1000)
    void updateMisspellings() {
        LOGGER.info("Start loading misspelling list from Wikipedia...");
        List<Misspelling> newMisspellingList = findWikipediaMisspellings();
        LOGGER.info("End parsing misspelling list from Wikipedia: {} items", newMisspellingList.size());

        if (!newMisspellingList.isEmpty()) {
            // Build a map to quick access the misspellings by word
            misspellingMap.clear();
            for (Misspelling misspelling : newMisspellingList) {
                if (misspelling.isCaseSensitive()) {
                    misspellingMap.put(misspelling.getWord(), misspelling);
                } else {
                    misspellingMap.put(misspelling.getWord(), misspelling);
                    misspellingMap.put(es.bvalero.replacer.utils.StringUtils.setFirstUpperCase(misspelling.getWord()), misspelling);
                }
            }

            // Build a long long regex with all the misspellings
            LOGGER.info("Start building automaton for misspelling alternations...");
            List<String> alternations = new ArrayList<>(newMisspellingList.size());
            for (Misspelling misspelling : newMisspellingList) {
                if (misspelling.isCaseSensitive()) {
                    alternations.add(misspelling.getWord());
                } else {
                    String word = misspelling.getWord();
                    String firstLetter = word.substring(0, 1);
                    String newWord = "[" + firstLetter + firstLetter.toUpperCase(Locale.forLanguageTag("es")) + "]" + word.substring(1);
                    alternations.add(newWord);
                }
            }
            String regexAlternations = "(" + StringUtils.join(alternations, "|") + ")";
            misspellingAlternationsAutomaton = new RunAutomaton(new RegExp(regexAlternations).toAutomaton());
            LOGGER.info("End building automaton for misspelling alternations");

            // Build an automaton with the misspellings starting with uppercase
            LOGGER.info("Start building automaton for uppercase misspellings...");
            List<String> uppercaseAlternations = new ArrayList<>(newMisspellingList.size());
            for (Misspelling misspelling : newMisspellingList) {
                if (misspelling.isCaseSensitive()
                        && es.bvalero.replacer.utils.StringUtils.startsWithUpperCase(misspelling.getWord())) {
                    uppercaseAlternations.add(misspelling.getWord());
                }
            }
            String regexUppercaseAlternations = "[\\.!\\*#\\|=]<Z>?("
                    + StringUtils.join(uppercaseAlternations, "|")
                    + ")";
            uppercaseMisspellingsAutomaton = new RunAutomaton(new RegExp(regexUppercaseAlternations)
                    .toAutomaton(new DatatypesAutomatonProvider()));
            LOGGER.info("End building automaton for uppercase misspellings");
        }
    }

    private List<Misspelling> findWikipediaMisspellings() {
        try {
            String misspellingListText = wikipediaFacade.getArticleContent(WikipediaFacade.MISSPELLING_LIST_ARTICLE);
            return parseMisspellingListText(misspellingListText);
        } catch (WikipediaException e) {
            LOGGER.error("Error loading misspellings list from Wikipedia", e);
            return new ArrayList<>();
        }
    }

    private List<Misspelling> parseMisspellingListText(String misspellingListText) {
        List<Misspelling> misspellings = new ArrayList<>(20000);

        try (InputStream stream = new ByteArrayInputStream(misspellingListText.getBytes(StandardCharsets.UTF_8));
             BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            Set<String> usedWords = new HashSet<>(20000);

            // Read file line by line
            String strLine;
            while ((strLine = br.readLine()) != null) {
                Misspelling misspelling = parseMisspellingLine(strLine);
                if (misspelling != null) {
                    String misspellingWord = misspelling.getWord();
                    if (usedWords.add(misspellingWord)) {
                        misspellings.add(misspelling);
                    } else {
                        LOGGER.warn("Duplicated misspelling term: {}", misspellingWord);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error parsing misspelling list", e);
        }

        return misspellings;
    }

    private Misspelling parseMisspellingLine(String misspellingLine) {
        Misspelling misspelling = null;

        String[] tokens = misspellingLine.split("\\|");
        // Ignore the lines not corresponding to misspelling lines
        if (!misspellingLine.isEmpty() && misspellingLine.startsWith(" ")) {
            if (tokens.length == 3) {
                boolean isCaseSensitive = ("cs".equalsIgnoreCase(tokens[1].trim()));
                String word = isCaseSensitive
                        ? tokens[0].trim()
                        : tokens[0].trim().toLowerCase(Locale.forLanguageTag("es"));
                String comment = tokens[2].trim();
                misspelling = new Misspelling(word, isCaseSensitive, comment);
            } else {
                LOGGER.warn("Bad formatted misspelling line: {}", misspellingLine);
            }
        }

        return misspelling;
    }

    /**
     * @return The misspelling related to the given word, or null if there is no such misspelling.
     */
    @Nullable
    public Misspelling findMisspellingByWord(@NotNull String word) {
        return getMisspellingMap().get(word);
    }

}
