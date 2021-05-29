package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.ListingLoader;
import es.bvalero.replacer.finder.listing.ParseFileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the cache for the false positive list in order to reduce the calls to Wikipedia.
 */
@Service
class FalsePositiveManager extends ParseFileManager<String> {

    @Autowired
    private ListingLoader listingLoader;

    @Override
    protected String getLabel() {
        return "False Positive";
    }

    @Override
    protected String findItemsTextInWikipedia(WikipediaLanguage lang) throws ReplacerException {
        return listingLoader.getFalsePositiveListPageContent(lang);
    }

    @Override
    protected String parseItemLine(String strLine) {
        return strLine.trim();
    }

    @Override
    protected String getItemKey(String falsePositive) {
        return falsePositive;
    }
}
