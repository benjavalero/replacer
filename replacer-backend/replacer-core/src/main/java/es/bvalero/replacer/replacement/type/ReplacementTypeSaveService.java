package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.finder.AddedTypeEvent;
import es.bvalero.replacer.finder.RemovedTypeEvent;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.replacement.save.ReplacementSaveRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
class ReplacementTypeSaveService {

    // Dependency injection
    private final ReplacementSaveRepository replacementSaveRepository;
    private final PageIndexService pageIndexService;

    ReplacementTypeSaveService(ReplacementSaveRepository replacementSaveRepository, PageIndexService pageIndexService) {
        this.replacementSaveRepository = replacementSaveRepository;
        this.pageIndexService = pageIndexService;
    }

    @EventListener
    public void onRemovedType(RemovedTypeEvent event) {
        replacementSaveRepository.removeByType(
            event.getReplacementType().getLang(),
            event.getReplacementType().getType()
        );
    }

    @EventListener
    public void onAddedType(AddedTypeEvent event) {
        this.pageIndexService.indexType(event.getReplacementType().getLang(), event.getReplacementType().getType());
    }
}
