package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.wikipedia.IWikipediaFacade;
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
class MisspellingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingManager.class);

    @Autowired
    private IWikipediaFacade wikipediaFacade;

    private List<Misspelling> misspellingList = new ArrayList<>();

    // Derived from the misspelling list to access faster by word
    private Map<String, Misspelling> misspellingMap = new HashMap<>();

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
        // TODO Externalize frequency to update the misspellings
        List<Misspelling> newMisspellingList = findWikipediaMisspellings();
        if (!newMisspellingList.isEmpty()) {
            misspellingList.clear();
            misspellingList.addAll(newMisspellingList);

            // Build a map to quick access the misspellings by word
            misspellingMap.clear();
            for (Misspelling misspelling : misspellingList) {
                misspellingMap.put(misspelling.getWord(), misspelling);
            }
        }
    }

    @NotNull
    private List<Misspelling> findWikipediaMisspellings() {
        List<Misspelling> misspellingList = new ArrayList<>();

        try {
            LOGGER.info("Start loading misspelling list from Wikipedia...");

            String misspellingListText = wikipediaFacade.getArticleContent(
                    IWikipediaFacade.MISSPELLING_LIST_ARTICLE);
            misspellingList.addAll(parseMisspellingListText(misspellingListText));

            LOGGER.info("End loading misspelling list from Wikipedia: {} items", misspellingList.size());
        } catch (Exception e) {
            LOGGER.error("Error loading misspellings list from Wikipedia", e);
        }

        return misspellingList;
    }

    @NotNull
    List<Misspelling> parseMisspellingListText(@NotNull String misspellingListText) {
        List<Misspelling> misspellingList = new ArrayList<>();

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
                        misspellingList.add(misspelling);
                    } else {
                        LOGGER.warn("Duplicated misspelling term: {}", misspellingWord);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error parsing misspelling list", e);
        }

        return misspellingList;
    }

    @Nullable
    private Misspelling parseMisspellingLine(@NotNull String misspellingLine) {
        Misspelling misspelling = null;

        String[] tokens = misspellingLine.split("\\|");
        // Ignore the lines not corresponding to misspelling lines
        if (!misspellingLine.isEmpty() && misspellingLine.startsWith(" ")) {
            if (tokens.length == 3) {
                misspelling = new Misspelling();
                misspelling.setCaseSensitive("cs".equalsIgnoreCase(tokens[1].trim()));
                misspelling.setWord(misspelling.isCaseSensitive()
                        ? tokens[0].trim()
                        : tokens[0].trim().toLowerCase());
                misspelling.setComment(tokens[2].trim());

                misspelling.setSuggestions(
                        parseSuggestions(misspelling.getComment(), misspelling.getWord()));
            } else {
                LOGGER.warn("Bad formatted misspelling line: {}", misspellingLine);
            }
        }

        return misspelling;
    }

    @NotNull
    List<String> parseSuggestions(@NotNull String suggestionLine, @NotNull String mainWord) {
        List<String> suggestions = new ArrayList<>();

        String suggestionWithoutBrackets = suggestionLine.replaceAll("\\(.+?\\)", "");
        for (String suggestion : suggestionWithoutBrackets.split(",")) {
            String word = suggestion.trim();

            // Don't suggest the misspelling main word
            // TODO This could be removed when implementing several suggestions
            if (StringUtils.isNotBlank(word) && !word.equals(mainWord)) {
                suggestions.add(word);
            }
        }

        return suggestions;
    }

    /**
     * @return The misspelling related to the given word, or null if there is no such misspelling.
     */
    @Nullable
    Misspelling findMisspellingByWord(@NotNull String word) {
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
