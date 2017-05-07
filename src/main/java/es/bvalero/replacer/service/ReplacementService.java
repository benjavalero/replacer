package es.bvalero.replacer.service;

import es.bvalero.replacer.domain.Interval;
import es.bvalero.replacer.domain.Misspelling;
import es.bvalero.replacer.domain.Replacement;
import es.bvalero.replacer.domain.ReplacementBD;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ReplacementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplacementService.class);

    @Autowired
    private ReplacementDao replacementDao;

    @Autowired
    private MisspellingService misspellingService;

    public void deleteReplacementsByTitle(String title) {
        replacementDao.deleteReplacementsByTitle(title);
    }

    public Map<String, List<ReplacementBD>> findAllReviewedReplacements() {
        Map<String, List<ReplacementBD>> replacementMap = new HashMap<>();
        List<ReplacementBD> replacements = replacementDao.findAllReviewedReplacements();
        for (ReplacementBD replacement : replacements) {
            String title = replacement.getTitle();
            if (!replacementMap.containsKey(title)) {
                replacementMap.put(title, new ArrayList<ReplacementBD>());
            }
            replacementMap.get(title).add(replacement);
        }
        return replacementMap;
    }

    public void insertReplacements(List<ReplacementBD> replacements) {
        replacementDao.insertAll(replacements);
    }

    public ReplacementBD findRandomReplacementToFix() {
        return replacementDao.findRandomReplacementToFix();
    }

    /**
     * Find all the possible replacements in the text. Returns unique pairs <title, word>.
     * It excludes the replacements in the text exceptions.
     */
    public List<ReplacementBD> findReplacementsForDB(String title, String text) {
        List<ReplacementBD> replacements = new ArrayList<>();

        // Find all the words in the text
        Map<Integer, String> wordMap = RegExUtils.findWords(text);

        // Find all the exceptions in the text
        List<Interval> exceptionIntervals = RegExUtils.findExceptionIntervals(text);

        Set<String> wordMisspellings = new HashSet<>();
        for (Map.Entry<Integer, String> entry : wordMap.entrySet()) {
            // Check if it is a misspeling and not in an exception
            String word = entry.getValue();
            int ini = entry.getKey();
            int end = ini + word.length();
            Misspelling wordMisspelling = misspellingService.getWordMisspelling(word);
            Interval wordInterval = new Interval(ini, end);

            if (wordMisspelling != null && !wordInterval.isContained(exceptionIntervals)) {
                wordMisspellings.add(wordMisspelling.getWord());
            }
        }

        for (String wordMisspelling : wordMisspellings) {
            ReplacementBD replacement = new ReplacementBD(title, wordMisspelling);
            replacements.add(replacement);
        }

        return replacements;
    }

    /* Find all the replacements in the text but the ones in exceptions */
    public List<Replacement> findReplacements(String text) {
        List<Replacement> replacements = new ArrayList<>();

        // Find all the words in the text
        Map<Integer, String> wordMap = RegExUtils.findWords(text);

        // Find all the exceptions in the text
        List<Interval> exceptionIntervals = RegExUtils.findExceptionIntervals(text);

        for (Map.Entry<Integer, String> entry : wordMap.entrySet()) {
            // Check if it is a misspeling and not in an exception
            String word = entry.getValue();
            int ini = entry.getKey();
            int end = ini + word.length();
            Misspelling wordMisspelling = misspellingService.getWordMisspelling(word);
            Interval wordInterval = new Interval(ini, end);

            if (wordMisspelling != null && !wordInterval.isContained(exceptionIntervals)) {
                Replacement replacement = new Replacement(ini, word);
                replacement.setFix(getFixForReplacement(word, wordMisspelling));
                replacement.setExplain(wordMisspelling.getSuggestion());
                replacements.add(replacement);
            }
        }

        return replacements;
    }

    private String getFixForReplacement(final String word, Misspelling misspelling) {
        // In case of error return the same original word
        // TODO For the moment I only take the first suggestion
        try {
            String misspellingFix = misspelling.getSuggestion().split("[,(]")[0].trim();
            if (!misspelling.isCaseSensitive() && StringUtils.startsWithUpperCase(word)) {
                misspellingFix = StringUtils.setFirstUpperCase(misspellingFix);
            }
            return misspellingFix;
        } catch (Exception e) {
            LOGGER.error("Error when getting fix for replacement." +
                            "\nORIGINAL WORD: {}\nMISSPELLING: {}",
                    word, misspelling, e);
            return word + " (ERROR)";
        }
    }

    public void setArticleAsReviewed(String title) {
        replacementDao.setArticleAsReviewed(title);
    }

    public Integer countMisspellings() {
        return replacementDao.countMisspellings();
    }

    public Integer countArticles() {
        return replacementDao.countArticles();
    }

}
