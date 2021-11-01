package es.bvalero.replacer.finder.listing.find;

import es.bvalero.replacer.common.FileUtils;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.domain.ReplacerException;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@VisibleForTesting
@Component
@Profile("offline")
public class ListingOfflineFinder implements ListingFinder {

    @Override
    public String getSimpleMisspellingListing(WikipediaLanguage lang) throws ReplacerException {
        switch (lang) {
            case SPANISH:
                return FileUtils.getFileContent("/offline/misspelling-list-es.txt");
            case GALICIAN:
                return FileUtils.getFileContent("/offline/misspelling-list-gl.txt");
            default:
                throw new IllegalCallerException();
        }
    }

    @Override
    public String getFalsePositiveListing(WikipediaLanguage lang) throws ReplacerException {
        switch (lang) {
            case SPANISH:
                return FileUtils.getFileContent("/offline/false-positives-es.txt");
            case GALICIAN:
                return FileUtils.getFileContent("/offline/false-positives-gl.txt");
            default:
                throw new IllegalCallerException();
        }
    }

    @Override
    public String getComposedMisspellingListing(WikipediaLanguage lang) throws ReplacerException {
        switch (lang) {
            case SPANISH:
                return FileUtils.getFileContent("/offline/composed-misspellings-es.txt");
            case GALICIAN:
                return FileUtils.getFileContent("/offline/composed-misspellings-gl.txt");
            default:
                throw new IllegalCallerException();
        }
    }
}
