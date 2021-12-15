package es.bvalero.replacer.page.list;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class ReviewByTypeService {

    @Autowired
    private ReplacementRepository replacementRepository;

    void reviewAsSystemByType(WikipediaLanguage lang, String type, String subtype) {
        // These reviewed replacements will be cleaned up in the next dump indexing
        replacementRepository.updateReviewerByType(lang, type, subtype, REVIEWER_SYSTEM);
    }
}
