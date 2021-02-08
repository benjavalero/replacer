package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Report to Check-Wikipedia service (https://checkwiki.toolforge.org/)
 * that a page fix has been done by Replacer.
 */
@Service
class CheckWikipediaService {

    @Autowired
    RestTemplate restTemplate;

    private static final String CHECK_WIKIPEDIA_URL = "https://checkwiki.toolforge.org/cgi-bin/checkwiki.cgi";

    void reportFix(WikipediaLanguage lang, String pageTitle, int fixId) {
        String project = String.format("%swiki", lang.getCode());

        String url = CHECK_WIKIPEDIA_URL + "?project=" + project + "&view=only&id=" + fixId + "&title=" + pageTitle;
        restTemplate.getForObject(url, String.class);
    }
}
