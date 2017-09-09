package es.bvalero.replacer.parse;

import es.bvalero.replacer.domain.ReplacementBD;
import es.bvalero.replacer.service.ReplacementService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

class FindMisspellingsHandler implements ArticleHandler {

    @Autowired
    private ReplacementService replacementService;

    // Pre-load all articles already reviewed in database to reduce queries
    private Map<String, List<ReplacementBD>> reviewedReplacements;

    @Override
    public void processArticle(String articleContent, String articleTitle, Date articleTimestamp) {
        // If the article was reviewed, and it was not before the current articleTimestamp, do nothing.
        if (isReviewedAfter(articleTitle, articleTimestamp)) {
            return;
        }

        // Find the possible replacements
        List<ReplacementBD> replacementList = replacementService.findReplacementsForDB(articleTitle, articleContent);

        if (isReviewed(articleTitle)) {
            // If the article is already reviewed, only add the NEW replacements
            replacementList.removeAll(getReviewedReplacements().get(articleTitle));
            replacementService.insertReplacements(replacementList);
        } else {
            // For the rest of cases, replace (delete and insert) all replacements
            replacementService.deleteReplacementsByTitle(articleTitle);
            replacementService.insertReplacements(replacementList);
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

    private Map<String, List<ReplacementBD>> getReviewedReplacements() {
        if (reviewedReplacements == null) {
            reviewedReplacements = replacementService.findAllReviewedReplacements();
        }
        return reviewedReplacements;
    }

}
