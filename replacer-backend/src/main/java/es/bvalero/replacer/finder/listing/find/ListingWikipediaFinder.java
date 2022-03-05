package es.bvalero.replacer.finder.listing.find;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!offline")
class ListingWikipediaFinder implements ListingFinder {

    @Autowired
    private WikipediaPageRepository wikipediaPageRepository;

    @Resource
    private Map<String, String> simpleMisspellingPages;

    @Resource
    private Map<String, String> composedMisspellingPages;

    @Resource
    private Map<String, String> falsePositivePages;

    @Override
    public String getSimpleMisspellingListing(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaPageRepository
            .findByTitle(lang, simpleMisspellingPages.get(lang.getCode()))
            .orElseThrow(ReplacerException::new)
            .getContent();
    }

    @Override
    public String getFalsePositiveListing(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaPageRepository
            .findByTitle(lang, falsePositivePages.get(lang.getCode()))
            .orElseThrow(ReplacerException::new)
            .getContent();
    }

    @Override
    public String getComposedMisspellingListing(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaPageRepository
            .findByTitle(lang, composedMisspellingPages.get(lang.getCode()))
            .orElseThrow(ReplacerException::new)
            .getContent();
    }
}
