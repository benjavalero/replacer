package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MisspellingComposedManager extends MisspellingManager {
    @Autowired
    private WikipediaService wikipediaService;

    @Override
    String getLabel() {
        return "Composed Misspelling";
    }

    @Override
    String findItemsText(WikipediaLanguage lang) {
        String text = FinderUtils.STRING_EMPTY;
        try {
            text = wikipediaService.getComposedMisspellingListPageContent(lang);
        } catch (ReplacerException e) {
            LOGGER.error("Error updating {} {} set", getLabel(), lang, e);
        }
        return text;
    }
}
