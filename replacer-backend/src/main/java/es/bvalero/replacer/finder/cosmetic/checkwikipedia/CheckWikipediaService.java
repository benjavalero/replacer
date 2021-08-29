package es.bvalero.replacer.finder.cosmetic.checkwikipedia;

import es.bvalero.replacer.common.WikipediaLanguage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Report to Check-Wikipedia service (https://checkwiki.toolforge.org/)
 * that a page fix has been done by Replacer.
 */
@Service
public class CheckWikipediaService {

    @Autowired
    RestTemplate restTemplate;

    private static final String CHECK_WIKIPEDIA_URL = "https://checkwiki.toolforge.org/cgi-bin/checkwiki.cgi";

    public void reportFix(WikipediaLanguage lang, String pageTitle, CheckWikipediaAction action) {
        String project = String.format("%swiki", lang.getCode());

        String url = String.format(
            "%s?project=%s&view=only&id=%d&title=%s",
            CHECK_WIKIPEDIA_URL,
            project,
            action.getValue(),
            pageTitle
        );
        restTemplate.getForObject(url, String.class);
    }
}
