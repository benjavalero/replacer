package es.bvalero.replacer.replacement;

import static es.bvalero.replacer.replacement.IndexedReplacement.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service to manage the persistence of the replacements,
 * as an intermediate layer between controllers and repositories.
 */
@Slf4j
@Service
public class ReplacementSaveService {

    // Dependency injection
    private final ReplacementSaveRepository replacementSaveRepository;

    public ReplacementSaveService(ReplacementSaveRepository replacementSaveRepository) {
        this.replacementSaveRepository = replacementSaveRepository;
    }

    /** Set as reviewed (by the system) all the replacements of the given type to review */
    public void updateSystemReviewerByType(WikipediaLanguage lang, StandardType type) {
        replacementSaveRepository.updateReviewerByType(lang, type, REVIEWER_SYSTEM);
    }
}
