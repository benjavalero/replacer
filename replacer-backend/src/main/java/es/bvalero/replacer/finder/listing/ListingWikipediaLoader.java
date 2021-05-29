package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!offline")
class ListingWikipediaLoader implements ListingLoader {

    @Autowired
    private WikipediaService wikipediaService;

    @Resource
    private Map<String, String> simpleMisspellingPages;

    @Resource
    private Map<String, String> composedMisspellingPages;

    @Resource
    private Map<String, String> falsePositivePages;

    @Override
    public String getMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaService
            .getPageByTitle(simpleMisspellingPages.get(lang.getCode()), lang)
            .orElseThrow(ReplacerException::new)
            .getContent();
    }

    @Override
    public String getFalsePositiveListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaService
            .getPageByTitle(falsePositivePages.get(lang.getCode()), lang)
            .orElseThrow(ReplacerException::new)
            .getContent();
    }

    @Override
    public String getComposedMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaService
            .getPageByTitle(composedMisspellingPages.get(lang.getCode()), lang)
            .orElseThrow(ReplacerException::new)
            .getContent();
    }
}
