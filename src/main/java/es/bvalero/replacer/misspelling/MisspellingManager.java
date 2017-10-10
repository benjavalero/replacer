package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
class MisspellingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingManager.class);

    @Autowired
    private IWikipediaFacade wikipediaService;

    private Map<String, Misspelling> misspellingMap = null;

    private Map<String, Misspelling> getMisspellingMap() {
        if (this.misspellingMap == null) {
            this.misspellingMap = findMisspellingsFromWikipedia();
        }
        return this.misspellingMap;
    }

    // Reload daily the misspellings from Wikipedia
    @Scheduled(fixedDelay = 3600 * 24 * 1000, initialDelay = 3600 * 24 * 1000)
    void updateMisspellings() {
        this.misspellingMap = findMisspellingsFromWikipedia();
    }

    /* Load the article in Spanish Wikipedia containing the most common misspellings */
    private Map<String, Misspelling> findMisspellingsFromWikipedia() {
        Map<String, Misspelling> misspellings = new TreeMap<>();

        LOGGER.info("Loading misspelling list from Wikipedia...");
        try {
            String misspellingListText = wikipediaService.getArticleContent(IWikipediaFacade.MISSPELLING_LIST_ARTICLE);
            List<Misspelling> misspellingList = parseMisspellingList(misspellingListText);
            for (Misspelling misspelling : misspellingList) {
                misspellings.put(misspelling.getWord(), misspelling);
            }

            LOGGER.info("Completed misspelling load: {} items", misspellings.size());
        } catch (Exception e) {
            LOGGER.error("Error loading misspellings from Wikipedia", e);
        }

        return misspellings;
    }

    List<Misspelling> parseMisspellingList(String misspellingListText) {
        List<Misspelling> misspellingList = new ArrayList<>();

        try (InputStream stream = new ByteArrayInputStream(misspellingListText.getBytes(StandardCharsets.UTF_8));
             BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            // Read file line by line
            Map<String, Misspelling> usedWordsMap = new TreeMap<>();
            String strLine;
            while ((strLine = br.readLine()) != null) {
                Misspelling lineMisspellings = parseMisspellingLine(strLine);
                if (lineMisspellings != null) {
                    String word = lineMisspellings.getWord();
                    if (usedWordsMap.containsKey(word)) {
                        LOGGER.warn("Duplicated misspelling term: {}", word);
                    } else {
                        usedWordsMap.put(word, lineMisspellings);
                    }
                }
            }

            misspellingList.addAll(usedWordsMap.values());
        } catch (IOException e) {
            LOGGER.error("Error parsing misspelling list", e);
        }

        return misspellingList;
    }

    private Misspelling parseMisspellingLine(String misspellingLine) {
        Misspelling misspelling = null;

        String[] tokens = misspellingLine.split("\\|");
        // Ignore the lines not corresponding to misspelling lines
        if (!misspellingLine.isEmpty() && misspellingLine.startsWith(" ")) {
            if (tokens.length == 3) {
                String wordToken = tokens[0].trim();
                boolean isCaseSensitive = "cs".equalsIgnoreCase(tokens[1].trim());
                String comment = tokens[2].trim();
                String word = isCaseSensitive
                        ? wordToken
                        : wordToken.toLowerCase(Locale.forLanguageTag("es"));
                List<String> suggestions = parseSuggestions(comment, word);
                misspelling = new Misspelling(word, isCaseSensitive, comment, suggestions);
            } else {
                LOGGER.warn("Bad formatted misspelling line: {}", misspellingLine);
            }
        }

        return misspelling;
    }

    List<String> parseSuggestions(String suggestionLine, String mainWord) {
        List<String> suggestions = new ArrayList<>();

        for (String suggestion : suggestionLine.split(",")) {
            String word;
            if (suggestion.contains("(")) {
                word = suggestion.substring(0, suggestion.indexOf("(")).trim();
            } else {
                word = suggestion.trim();
            }

            // Don't suggest the misspelling main word
            if (StringUtils.hasText(word) && !word.equals(mainWord)) {
                suggestions.add(word);
            }
        }

        return suggestions;
    }

    /**
     * Find the misspelling related to a given word.
     *
     * @param word The word which may be a misspellings.
     * @return The related misspelling or null.
     */
    Misspelling findMisspellingByWord(String word) {
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
