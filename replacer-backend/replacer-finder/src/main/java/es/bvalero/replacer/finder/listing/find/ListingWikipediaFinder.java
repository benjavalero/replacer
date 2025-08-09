package es.bvalero.replacer.finder.listing.find;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!offline")
class ListingWikipediaFinder implements ListingFinder {

    // Dependency injection
    // TODO: Try to remove the dependency on this repository in order to make it package-private
    private final WikipediaPageRepository wikipediaPageRepository;
    private final FinderProperties finderProperties;

    ListingWikipediaFinder(WikipediaPageRepository wikipediaPageRepository, FinderProperties finderProperties) {
        this.wikipediaPageRepository = wikipediaPageRepository;
        this.finderProperties = finderProperties;
    }

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
