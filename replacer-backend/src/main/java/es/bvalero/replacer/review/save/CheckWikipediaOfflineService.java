package es.bvalero.replacer.review.save;

import es.bvalero.replacer.common.domain.CheckWikipediaAction;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("offline")
@Service
class CheckWikipediaOfflineService implements CheckWikipediaService {

    @Override
    public void reportFix(WikipediaLanguage lang, String pageTitle, CheckWikipediaAction action) {
        // Do nothing
    }
}
