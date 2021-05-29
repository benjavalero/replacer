package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;

public interface ListingLoader {
    String getMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException;

    String getFalsePositiveListPageContent(WikipediaLanguage lang) throws ReplacerException;

    String getComposedMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException;
}
