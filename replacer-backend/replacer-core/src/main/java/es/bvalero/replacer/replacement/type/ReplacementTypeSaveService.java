package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.find.WikipediaPageRepository;
import es.bvalero.replacer.page.find.WikipediaSearchRequest;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.replacement.save.ReplacementSaveRepository;
import java.util.Collection;
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

    // TODO: Maybe this could be called directly from finder to repository module
    @Override
    public void remove(WikipediaLanguage lang, StandardType type) {
        replacementSaveRepository.removeByType(lang, type);
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
        wikipediaPageRepository.findByKeys(pageKeys).forEach(pageIndexService::indexPage);
    }
}
