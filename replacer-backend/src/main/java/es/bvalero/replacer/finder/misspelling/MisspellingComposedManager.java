package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MisspellingComposedManager extends MisspellingManager {

    @Autowired
    private WikipediaService wikipediaService;

    @Override
    String getLabel() {
        return "Composed Misspelling";
    }

    String getType() {
        return MisspellingComposedFinder.TYPE_MISSPELLING_COMPOSED;
    }

    @Override
    String findItemsTextInWikipedia(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaService.getComposedMisspellingListPageContent(lang);
    }
}
