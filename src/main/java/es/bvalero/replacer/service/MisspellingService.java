package es.bvalero.replacer.service;

import es.bvalero.replacer.domain.Misspelling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class MisspellingService {

    static final String MISSPELLING_LIST_ARTICLE = "Wikipedia:Corrector_ortográfico/Listado";
    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingService.class);
    private final Map<String, Misspelling> misspellingMap = new TreeMap<>();
    @Autowired
    private IWikipediaService wikipediaService;

    Map<String, Misspelling> getMisspellingMap() {
        if (misspellingMap.isEmpty()) {
            updateMisspellingList();
        }
        return misspellingMap;
    }

    public void updateMisspellingList() {
        LOGGER.info("Loading misspelling list from Wikipedia...");
        List<Misspelling> misspellingList = getMisspellingListFromWikipedia();

        // Update the case-sensitive map
        misspellingMap.clear();
        for (Misspelling misspelling : misspellingList) {
            misspellingMap.put(misspelling.getWord(), misspelling);
        }

        LOGGER.info("Completed misspelling load: {} items", misspellingList.size());
    }

    private List<Misspelling> getMisspellingListFromWikipedia() {
        String articleContent = wikipediaService.getArticleContent(MISSPELLING_LIST_ARTICLE);
        return parseMisspellingList(articleContent);
    }

    List<Misspelling> parseMisspellingList(String misspellingListText) {
        List<Misspelling> misspellingList = new ArrayList<>();

        try (InputStream stream = new ByteArrayInputStream(misspellingListText.getBytes(StandardCharsets.UTF_8));
             BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            // Read File Line By Line
            Map<String, Misspelling> missMap = new TreeMap<>();
            String strLine;
            while ((strLine = br.readLine()) != null) {
                Misspelling miss = parseMisspellingLine(strLine);
                if (miss != null) {
                    String word = miss.getWord();
                    if (missMap.containsKey(word)) {
                        LOGGER.warn("Duplicated misspelling term: {}", word);
                    } else {
                        missMap.put(word, miss);
                    }
                }
            }

            misspellingList.addAll(missMap.values());
        } catch (IOException e) {
            LOGGER.error("Error parsing misspelling list", e);
        }

        return misspellingList;
    }

    private Misspelling parseMisspellingLine(final String misspellingLine) {
        Misspelling misspelling = null;
        String[] tokens = misspellingLine.split("\\|");
        if (!misspellingLine.isEmpty() && misspellingLine.startsWith(" ")) {
            if (tokens.length == 3) {
                misspelling = new Misspelling();

                String theWord = tokens[0].trim();
                boolean isCaseSensitive = "cs".equalsIgnoreCase(tokens[1].trim());
                misspelling.setWord(isCaseSensitive ? theWord : theWord.toLowerCase(Locale.forLanguageTag("es")));
                misspelling.setCaseSensitive(isCaseSensitive);
                misspelling.setSuggestion(tokens[2].trim());
            } else {
                LOGGER.warn("Bad formatted misspelling line: {}", misspellingLine);
            }
        }

        return misspelling;
    }

    /* If the word is a misspelling it will return the word in the misspelling (lower/upper) case.
     If not, return null. */
    public Misspelling getWordMisspelling(final String word) {
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
