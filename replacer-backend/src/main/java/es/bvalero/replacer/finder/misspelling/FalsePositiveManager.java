package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Manages the cache for the false positive list in order to reduce the calls to Wikipedia.
 */
@Service
public class FalsePositiveManager extends ParseFileManager<String> {

    @Autowired
    private WikipediaService wikipediaService;

    @Override
    void processRemovedItems(Set<String> oldItems, Set<String> newItems) {
        // Nothing to do
    }

    @Override
    String getLabel() {
        return "False Positive";
    }

    @Override
    String findItemsText() throws WikipediaException {
        return wikipediaService.getFalsePositiveListPageContent();
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
