package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;

public interface ListingContentService {
    String getMisspellingListingContent(WikipediaLanguage lang) throws ReplacerException;

    String getFalsePositiveListingContent(WikipediaLanguage lang) throws ReplacerException;

    String getComposedMisspellingListingContent(WikipediaLanguage lang) throws ReplacerException;
}
