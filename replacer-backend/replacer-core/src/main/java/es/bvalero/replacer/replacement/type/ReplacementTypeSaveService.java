package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.AddedTypeEvent;
import es.bvalero.replacer.finder.RemovedTypeEvent;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.replacement.save.ReplacementSaveRepository;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import es.bvalero.replacer.wikipedia.WikipediaSearchRequest;
import java.util.Collection;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
class ReplacementTypeSaveService implements ReplacementTypeSaveApi {

    // Dependency injection
    private final ReplacementSaveRepository replacementSaveRepository;
    private final WikipediaPageRepository wikipediaPageRepository;
    private final PageIndexService pageIndexService;

    ReplacementTypeSaveService(
        ReplacementSaveRepository replacementSaveRepository,
        WikipediaPageRepository wikipediaPageRepository,
        PageIndexService pageIndexService
    ) {
        this.replacementSaveRepository = replacementSaveRepository;
        this.wikipediaPageRepository = wikipediaPageRepository;
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
        index(event.getReplacementType().getLang(), event.getReplacementType().getType());
    }

    @Override
    public void index(WikipediaLanguage lang, StandardType type) {
        // For each type, find the Wikipedia pages containing the text, using the default search options.
        // At least some of them not to overload the Wikipedia search.
        WikipediaSearchRequest request = WikipediaSearchRequest.builder().lang(lang).text(type.getSubtype()).build();
        Collection<PageKey> pageKeys = wikipediaPageRepository
            .findByContent(request)
            .getPageIds()
            .stream()
            .map(pageId -> PageKey.of(lang, pageId))
            .toList();
        try {
            wikipediaPageRepository.findByKeys(pageKeys).forEach(pageIndexService::indexPage);
        } catch (WikipediaException e) {
            // Do nothing, simply we don't index in case we cannot retrieve the pages.
        }
    }
}
