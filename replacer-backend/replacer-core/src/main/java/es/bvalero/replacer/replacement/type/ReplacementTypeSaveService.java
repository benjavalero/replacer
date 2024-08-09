package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.find.WikipediaPageRepository;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.replacement.ReplacementSaveRepository;
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
        // For each type, find the Wikipedia pages containing the text.
        // At least some of them not to overload the Wikipedia search.
        wikipediaPageRepository.findByContent(lang, type.getSubtype()).forEach(pageIndexService::indexPage);
    }
}
