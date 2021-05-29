package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.common.ListingLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class MisspellingComposedManager extends MisspellingManager {

    @Autowired
    private ListingLoader listingLoader;

    @Override
    protected String getLabel() {
        return "Composed Misspelling";
    }

    @Override
    String getType() {
        return ReplacementType.MISSPELLING_COMPOSED;
    }

    @Override
    protected String findItemsTextInWikipedia(WikipediaLanguage lang) throws ReplacerException {
        return listingLoader.getComposedMisspellingListPageContent(lang);
    }
}
