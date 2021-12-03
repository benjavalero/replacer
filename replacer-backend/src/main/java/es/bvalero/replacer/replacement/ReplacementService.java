package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ReplacementService {

    @Autowired
    private ReplacementDao replacementDao;

    @Autowired
    private ReplacementStatsDao replacementStatsDao;

    @Autowired
    private CustomDao customDao;

    ///// CRUD

    public void insert(CustomEntity entity) {
        customDao.insert(entity);
    }

    ///// PAGE REVIEW

    @Loggable(value = Loggable.DEBUG, prepend = true)
    public List<Integer> findPageIdsReviewedByReplacement(WikipediaLanguage lang, String replacement, boolean cs) {
        return customDao.findPageIdsReviewed(lang, replacement, cs);
    }

    public void reviewByPageId(
        WikipediaLanguage lang,
        int pageId,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    ) {
        replacementStatsDao.reviewByPageId(lang, pageId, type, subtype, reviewer);
    }

    ///// STATISTICS

    long countReplacementsReviewed(WikipediaLanguage lang) {
        return replacementStatsDao.countReplacementsReviewed(lang);
    }

    public long countReplacementsNotReviewed(WikipediaLanguage lang) {
        return replacementStatsDao.countReplacementsNotReviewed(lang);
    }

    List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        return replacementStatsDao.countReplacementsGroupedByReviewer(lang);
    }

    List<TypeCount> countReplacementsGroupedByType(WikipediaLanguage lang) throws ReplacerException {
        return replacementStatsDao.countReplacementsGroupedByType(lang).getTypeCounts();
    }

    ///// MISSPELLING MANAGER

    public void deleteToBeReviewedBySubtype(WikipediaLanguage lang, String type, Set<String> subtypes) {
        replacementDao.deleteToBeReviewedBySubtype(lang, type, subtypes);
    }
}
