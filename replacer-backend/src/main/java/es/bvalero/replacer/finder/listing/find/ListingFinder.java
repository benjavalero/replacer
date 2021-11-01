package es.bvalero.replacer.finder.listing.find;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.domain.ReplacerException;

public interface ListingFinder {
    String getSimpleMisspellingListing(WikipediaLanguage lang) throws ReplacerException;

    String getFalsePositiveListing(WikipediaLanguage lang) throws ReplacerException;

    String getComposedMisspellingListing(WikipediaLanguage lang) throws ReplacerException;
}
