package es.bvalero.replacer.parse;

import es.bvalero.replacer.domain.Interval;
import es.bvalero.replacer.domain.Misspelling;
import es.bvalero.replacer.persistence.ReplacementDao;
import es.bvalero.replacer.persistence.pojo.ReplacementDb;
import es.bvalero.replacer.service.MisspellingService;
import es.bvalero.replacer.utils.RegExUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.*;

class FindMisspellingsHandler implements ArticleHandler {

    @Autowired
    private ReplacementDao replacementDao;

    @Autowired
    private MisspellingService misspellingService;

    // Pre-load all articles already reviewed in database to reduce queries
    private Map<String, List<ReplacementDb>> reviewedReplacements;

    @Override
    public void processArticle(String articleContent, String articleTitle, Date articleTimestamp) {
        // If the article was reviewed, and it was not before the current articleTimestamp, do nothing.
        if (isReviewedAfter(articleTitle, articleTimestamp)) {
            return;
        }

        // TODO Create an interface implemented by the replacement finders, not only word replacements.
        Set<ReplacementDb> replacements = findWordReplacements(articleTitle, articleContent);

        if (isReviewed(articleTitle)) {
            // If the article is already reviewed, only add the NEW replacements
            replacements.removeAll(getReviewedReplacements().get(articleTitle));
            replacementDao.insertAll(new ArrayList<>(replacements));
        } else {
            // For the rest of cases, replace (delete and insert) all replacements
            replacementDao.deleteReplacementsByTitle(articleTitle);
            replacementDao.insertAll(new ArrayList<>(replacements));
        }
    }

    boolean isReviewedAfter(String articleTitle, Date articleTimestamp) {
        if (isReviewed(articleTitle)) {
            Timestamp lastReviewed = this.getReviewedReplacements().get(articleTitle).get(0).getLastReviewed();
            return !(lastReviewed.before(articleTimestamp));
        } else {
            return false;
        }
    }

    private boolean isReviewed(String articleTitle) {
        return this.getReviewedReplacements().containsKey(articleTitle);
    }

    /* Build a map with all the reviewed articles and its replacements */
    private Map<String, List<ReplacementDb>> getReviewedReplacements() {
        if (this.reviewedReplacements == null) {
            List<ReplacementDb> replacements = replacementDao.findAllReviewedReplacements();

            this.reviewedReplacements = new HashMap<>();
            for (ReplacementDb replacement : replacements) {
                String title = replacement.getTitle();
                if (!this.reviewedReplacements.containsKey(title)) {
                    this.reviewedReplacements.put(title, new ArrayList<ReplacementDb>());
                }
                this.reviewedReplacements.get(title).add(replacement);
            }
        }
        return this.reviewedReplacements;
    }

    Set<ReplacementDb> findWordReplacements(String title, String text) {
        Set<ReplacementDb> replacements = new HashSet<>();

        // Find all the words in the text
        Map<Integer, String> wordMap = RegExUtils.findWords(text);

        // Find all the exceptions in the text
        List<Interval> exceptionIntervals = RegExUtils.findExceptionIntervals(text);

        for (Map.Entry<Integer, String> entry : wordMap.entrySet()) {
            // Check if it is a misspelling and not in an exception
            String word = entry.getValue();
            int ini = entry.getKey();
            int end = ini + word.length();
            Misspelling wordMisspelling = misspellingService.getWordMisspelling(word);
            Interval wordInterval = new Interval(ini, end);

            if (wordMisspelling != null && !wordInterval.isContained(exceptionIntervals)) {
                replacements.add(new ReplacementDb(title, wordMisspelling.getWord()));
            }
        }

        return replacements;
    }

}
