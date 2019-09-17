package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComposedManager extends MisspellingManager {

    @Autowired
    private WikipediaService wikipediaService;

    @Override
    String findItemsText() throws WikipediaException {
        return wikipediaService.getComposedMisspellingListPageContent();
    }

}
