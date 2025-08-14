package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.finder.RemovedTypeEvent;
import es.bvalero.replacer.replacement.ReplacementSaveRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
class ReplacementTypeSaveService {

    // Dependency injection
    private final ReplacementSaveRepository replacementSaveRepository;

    ReplacementTypeSaveService(ReplacementSaveRepository replacementSaveRepository) {
        this.replacementSaveRepository = replacementSaveRepository;
    }

    @EventListener
    public void onRemovedType(RemovedTypeEvent event) {
        replacementSaveRepository.removeByType(event.getLang(), event.getType());
    }
}
