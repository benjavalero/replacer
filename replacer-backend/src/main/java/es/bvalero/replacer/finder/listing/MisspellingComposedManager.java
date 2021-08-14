package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import org.springframework.stereotype.Service;

@Service
public class MisspellingComposedManager extends MisspellingManager {

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
        return listingFinder.getComposedMisspellingListing(lang);
    }
}
