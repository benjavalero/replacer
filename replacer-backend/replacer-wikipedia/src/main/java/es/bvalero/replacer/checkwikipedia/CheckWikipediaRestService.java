package es.bvalero.replacer.checkwikipedia;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Profile("!offline")
@Service
class CheckWikipediaRestService {

    private static final String CHECK_WIKIPEDIA_URL = "https://checkwiki.toolforge.org/cgi-bin/checkwiki.cgi";

    // Dependency injection
    private final RestTemplate restTemplate;

    CheckWikipediaRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @EventListener
    public void onFix(CheckWikipediaFixEvent event) {
        CheckWikipediaAction action = event.getAction();
        if (action.isNoAction()) {
            return;
        }

        WikipediaLanguage lang = event.getLang();
        String project = String.format("%swiki", lang.getCode());

        String url = String.format(
            "%s?project=%s&view=only&id=%d&title=%s",
            CHECK_WIKIPEDIA_URL,
            project,
            action.getValue(),
            event.getPageTitle()
        );
        restTemplate.getForObject(url, String.class);
    }
}
