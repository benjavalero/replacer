package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the cache for the false positive list in order to reduce the calls to Wikipedia.
 */
@Slf4j
@Service
public class FalsePositiveManager extends ParseFileManager<String> {
    @Autowired
    private WikipediaService wikipediaService;

    @Override
    String getLabel() {
        return "False Positive";
    }

    @Override
    String findItemsText(WikipediaLanguage lang) {
        String text = FinderUtils.STRING_EMPTY;
        // TODO: Support all languages
        if (WikipediaLanguage.SPANISH == lang) {
            try {
                text = wikipediaService.getFalsePositiveListPageContent();
            } catch (ReplacerException e) {
                LOGGER.error("Error updating {} {} set", getLabel(), lang, e);
            }
        }
        return text;
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
