package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the cache for the false positive list in order to reduce the calls to Wikipedia.
 */
@Service
public class FalsePositiveManager extends ListingManager<String> {

    @Setter // For testing
    @Autowired
    private ListingContentService listingContentService;

    @Override
    protected String getLabel() {
        return "False Positive";
    }

    @Override
    protected String findItemsTextInWikipedia(WikipediaLanguage lang) throws ReplacerException {
        return listingContentService.getFalsePositiveListingContent(lang);
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
