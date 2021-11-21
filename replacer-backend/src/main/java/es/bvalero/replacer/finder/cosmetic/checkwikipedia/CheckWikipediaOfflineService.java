package es.bvalero.replacer.finder.cosmetic.checkwikipedia;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("offline")
@Service
public class CheckWikipediaOfflineService implements CheckWikipediaService {

    @Override
    public void reportFix(WikipediaLanguage lang, String pageTitle, CheckWikipediaAction action) {
        // Do nothing
    }
}
