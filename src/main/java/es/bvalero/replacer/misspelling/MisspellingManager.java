package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaFacade;
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

    @Autowired
    private IWikipediaFacade wikipediaFacade;

    // Derived from the misspelling list to access faster by word
    private Map<String, Misspelling> misspellingMap = new HashMap<>();

    @NotNull
    private Map<String, Misspelling> getMisspellingMap() {
        if (this.misspellingMap.isEmpty()) { // For the first time
            updateMisspellings();
        }
        return this.misspellingMap;
    }

    /**
     * Update daily the list of misspellings from Wikipedia.
     */
    @SuppressWarnings("WeakerAccess")
    @Scheduled(fixedDelay = 3600 * 24 * 1000, initialDelay = 3600 * 24 * 1000)
    void updateMisspellings() {
        List<Misspelling> newMisspellingList = findWikipediaMisspellings();
        if (!newMisspellingList.isEmpty()) {
            // Build a map to quick access the misspellings by word
            misspellingMap.clear();
            for (Misspelling misspelling : newMisspellingList) {
                misspellingMap.put(misspelling.getWord(), misspelling);
            }
        }
    }

    List<Misspelling> findWikipediaMisspellings() {
        List<Misspelling> misspellings = new ArrayList<>();

        try {
            LOGGER.info("Start loading misspelling list from Wikipedia...");

            String misspellingListText = wikipediaFacade.getArticleContent(
                    WikipediaFacade.MISSPELLING_LIST_ARTICLE);
            misspellings.addAll(parseMisspellingListText(misspellingListText));

            LOGGER.info("End loading misspelling list from Wikipedia: {} items", misspellings.size());
        } catch (WikipediaException e) {
            LOGGER.error("Error loading misspellings list from Wikipedia", e);
        }

        return misspellings;
    }

    private List<Misspelling> parseMisspellingListText(String misspellingListText) {
        List<Misspelling> misspellings = new ArrayList<>();

        try (InputStream stream = new ByteArrayInputStream(misspellingListText.getBytes(StandardCharsets.UTF_8));
             BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            Set<String> usedWords = new HashSet<>();

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
        Misspelling wordMisspelling = null;
        if (getMisspellingMap().containsKey(word)) {
            wordMisspelling = getMisspellingMap().get(word);
        } else {
            String lowerWord = word.toLowerCase(Locale.forLanguageTag("es"));
            if (getMisspellingMap().containsKey(lowerWord)
                    && !getMisspellingMap().get(lowerWord).isCaseSensitive()) {
                wordMisspelling = getMisspellingMap().get(lowerWord);
            }
        }
        return wordMisspelling;
    }

}
