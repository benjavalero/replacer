package es.bvalero.replacer.finder.listing.find;

import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!offline")
class ListingWikipediaFinder implements ListingFinder {

    @Autowired
    private WikipediaService wikipediaService;

    @Resource
    private Map<String, String> simpleMisspellingPages;

    @Resource
    private Map<String, String> composedMisspellingPages;

    @Resource
    private Map<String, String> falsePositivePages;

    @Override
    public String getSimpleMisspellingListing(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaService
            .getPageByTitle(lang, simpleMisspellingPages.get(lang.getCode()))
            .orElseThrow(ReplacerException::new)
            .getContent();
    }

    @Override
    public String getFalsePositiveListing(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaService
            .getPageByTitle(lang, falsePositivePages.get(lang.getCode()))
            .orElseThrow(ReplacerException::new)
            .getContent();
    }

    @Override
    public String getComposedMisspellingListing(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaService
            .getPageByTitle(lang, composedMisspellingPages.get(lang.getCode()))
            .orElseThrow(ReplacerException::new)
            .getContent();
    }
}
