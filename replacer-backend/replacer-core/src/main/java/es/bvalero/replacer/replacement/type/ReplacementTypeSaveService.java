package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.finder.RemovedTypeEvent;
import es.bvalero.replacer.page.PageSaveRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
class ReplacementTypeSaveService {

    // Dependency injection
    private final PageSaveRepository pageSaveRepository;

    ReplacementTypeSaveService(PageSaveRepository pageSaveRepository) {
        this.pageSaveRepository = pageSaveRepository;
    }

    @EventListener
    public void onRemovedType(RemovedTypeEvent event) {
        pageSaveRepository.removeByType(event.getLang(), event.getType());
    }
}
