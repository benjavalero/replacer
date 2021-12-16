package es.bvalero.replacer.page.list;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class ReviewByTypeService {

    @Autowired
    private ReplacementTypeRepository replacementTypeRepository;

    void reviewAsSystemByType(WikipediaLanguage lang, ReplacementType type) {
        // These reviewed replacements will be cleaned up in the next dump indexing
        replacementTypeRepository.updateReviewerByType(lang, type, REVIEWER_SYSTEM);
    }
}
