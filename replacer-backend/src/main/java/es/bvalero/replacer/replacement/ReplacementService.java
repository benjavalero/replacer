package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReplacementService {

    @Autowired
    private ReplacementDao replacementDao;

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

    ///// MISSPELLING MANAGER

    public void deleteToBeReviewedBySubtype(WikipediaLanguage lang, String type, Set<String> subtypes) {
        replacementDao.deleteToBeReviewedBySubtype(lang, type, subtypes);
    }
}
