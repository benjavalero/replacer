package es.bvalero.replacer.finder.listing.find;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;

public interface ListingFinder {
    String getSimpleMisspellingListing(WikipediaLanguage lang) throws ReplacerException;

    String getFalsePositiveListing(WikipediaLanguage lang) throws ReplacerException;

    String getComposedMisspellingListing(WikipediaLanguage lang) throws ReplacerException;
}
