package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class PageReplacementServiceImpl implements PageReplacementService {

    @Autowired
    private ReplacementService replacementService;

    @Override
    public List<ReplacementEntity> findByPageId(int pageId, WikipediaLanguage lang) {
        return replacementService.findByPageId(pageId, lang);
    }

    @Override
    public void finish(WikipediaLanguage lang) {
        // Do nothing
    }
}
