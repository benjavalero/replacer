package es.bvalero.replacer.finder.listing.find;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!offline")
class ListingWikipediaFinder implements ListingFinder {

    @Autowired
    private WikipediaPageRepository wikipediaPageRepository;

    @Autowired
    private FinderProperties finderProperties;

    @Override
    public String getSimpleMisspellingListing(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaPageRepository
            .findByTitle(lang, finderProperties.getSimpleMisspellingPages().get(lang.getCode()))
            .orElseThrow(ReplacerException::new)
            .getContent();
    }

    @Override
    public String getFalsePositiveListing(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaPageRepository
            .findByTitle(lang, finderProperties.getFalsePositivePages().get(lang.getCode()))
            .orElseThrow(ReplacerException::new)
            .getContent();
    }

    @Override
    public String getComposedMisspellingListing(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaPageRepository
            .findByTitle(lang, finderProperties.getComposedMisspellingPages().get(lang.getCode()))
            .orElseThrow(ReplacerException::new)
            .getContent();
    }
}
