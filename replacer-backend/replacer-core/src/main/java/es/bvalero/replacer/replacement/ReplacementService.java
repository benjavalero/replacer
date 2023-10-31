package es.bvalero.replacer.replacement;

import static es.bvalero.replacer.replacement.IndexedReplacement.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service to manage the persistence of the replacements, especially after indexing or reviewing.
 * All methods are grouped here as we maintain a cache for the count of unreviewed pages by replacement type.
 */
@Slf4j
@Service
public class ReplacementService {

    // Dependency injection
    private final ReplacementSaveRepository replacementSaveRepository;

    public ReplacementService(ReplacementSaveRepository replacementSaveRepository) {
        this.replacementSaveRepository = replacementSaveRepository;
    }

    public void addReplacements(Collection<IndexedReplacement> replacements) {
        replacementSaveRepository.add(replacements);
    }

    public void updateReplacements(Collection<IndexedReplacement> replacements) {
        replacementSaveRepository.update(replacements);
    }

    public void removeReplacements(Collection<IndexedReplacement> replacements) {
        replacementSaveRepository.remove(replacements);
    }

    public void reviewReplacementsByType(WikipediaLanguage lang, StandardType type) {
        replacementSaveRepository.updateReviewerByType(lang, type, REVIEWER_SYSTEM);
    }

    public void updateReviewer(Collection<IndexedReplacement> replacements) {
        replacementSaveRepository.updateReviewer(replacements);
    }

    void removeReplacementsByType(WikipediaLanguage lang, StandardType type) {
        replacementSaveRepository.removeByType(lang, type);
    }
}
