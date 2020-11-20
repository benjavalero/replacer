package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the cache for the false positive list in order to reduce the calls to Wikipedia.
 */
@Service
public class FalsePositiveManager extends ParseFileManager<String> {
    @Autowired
    private WikipediaService wikipediaService;

    @Override
    String getLabel() {
        return "False Positive";
    }

    @Override
    String findItemsTextInWikipedia(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaService.getFalsePositiveListPageContent(lang);
    }

    @Override
    String parseItemLine(String strLine) {
        return strLine.trim();
    }

    @Override
    String getItemKey(String falsePositive) {
        return falsePositive;
    }
}
