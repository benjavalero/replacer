package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.common.ParseFileManager;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the cache for the false positive list in order to reduce the calls to Wikipedia.
 */
@Service
class FalsePositiveManager extends ParseFileManager<String> {

    @Autowired
    private WikipediaService wikipediaService;

    @Override
    protected String getLabel() {
        return "False Positive";
    }

    @Override
    protected String findItemsTextInWikipedia(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaService.getFalsePositiveListPageContent(lang);
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
