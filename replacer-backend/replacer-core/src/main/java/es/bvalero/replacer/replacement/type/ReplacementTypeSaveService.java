package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.replacement.ReplacementSaveRepository;
import org.springframework.stereotype.Service;

@Service
class ReplacementTypeSaveService implements ReplacementTypeSaveApi {

    // Dependency injection
    private final ReplacementSaveRepository replacementSaveRepository;

    ReplacementTypeSaveService(ReplacementSaveRepository replacementSaveRepository) {
        this.replacementSaveRepository = replacementSaveRepository;
    }

    // TODO: Maybe this could be called directly from finder to repository module
    @Override
    public void remove(WikipediaLanguage lang, StandardType type) {
        replacementSaveRepository.removeByType(lang, type);
    }
}
