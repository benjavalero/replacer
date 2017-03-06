package es.bvalero.replacer.parse;

import es.bvalero.replacer.domain.ReplacementBD;
import es.bvalero.replacer.service.MisspellingService;
import es.bvalero.replacer.service.ReplacementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FindMisspellingsHandler extends ArticlesHandler {

    @Autowired
    private MisspellingService misspellingService;

    @Autowired
    private ReplacementService replacementService;

    @Value("${replacer.incremental.update}")
    private boolean incremental;

    // Pre-load all articles already reviewed in database to reduce queries
    private Map<String, List<ReplacementBD>> reviewedReplacements;

    @Override
    void processArticle() {
        // In case of incremental update, if the article is not reviewed
        // and it has not been modified since it was added to the database, do nothing
        if (isIncremental() && !isReviewed() && !isRecentlyUpdated()) {
            return;
        }

        // Find the possible replacements
        List<ReplacementBD> replacementList = replacementService.findReplacementsForDB(getCurrentTitle(), getCurrentText());

        if (isReviewed()) {
            // If the article is already reviewed, only add the NEW replacements
            replacementList.removeAll(getReviewedReplacements().get(getCurrentTitle()));
            replacementService.insertReplacements(replacementList);
        } else {
            // For the rest of cases, replace (delete and insert) all replacements
            replacementService.deleteReplacementsByTitle(getCurrentTitle());
            replacementService.insertReplacements(replacementList);
        }
    }

    boolean isIncremental() {
        return this.incremental;
    }

    void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

    boolean isReviewed() {
        return getReviewedReplacements().containsKey(getCurrentTitle());
    }

    boolean isRecentlyUpdated() {
        long diff = System.currentTimeMillis() - getCurrentTimestamp().getTime();
        long days = diff / (1000 * 60 * 60 * 24);
        return days < 30;
    }

    private Map<String, List<ReplacementBD>> getReviewedReplacements() {
        if (reviewedReplacements == null) {
            reviewedReplacements = replacementService.findAllReviewedReplacements();
        }
        return reviewedReplacements;
    }

}
