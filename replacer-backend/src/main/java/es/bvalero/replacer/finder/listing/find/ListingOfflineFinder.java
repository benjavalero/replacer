package es.bvalero.replacer.finder.listing.find;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.common.util.FileOfflineUtils;
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
                return FileOfflineUtils.getFileContent("/offline/misspelling-list-es.txt");
            case GALICIAN:
                return FileOfflineUtils.getFileContent("/offline/misspelling-list-gl.txt");
            default:
                throw new IllegalCallerException();
        }
    }

    @Override
    public String getFalsePositiveListing(WikipediaLanguage lang) throws ReplacerException {
        switch (lang) {
            case SPANISH:
                return FileOfflineUtils.getFileContent("/offline/false-positives-es.txt");
            case GALICIAN:
                return FileOfflineUtils.getFileContent("/offline/false-positives-gl.txt");
            default:
                throw new IllegalCallerException();
        }
    }

    @Override
    public String getComposedMisspellingListing(WikipediaLanguage lang) throws ReplacerException {
        switch (lang) {
            case SPANISH:
                return FileOfflineUtils.getFileContent("/offline/composed-misspellings-es.txt");
            case GALICIAN:
                return FileOfflineUtils.getFileContent("/offline/composed-misspellings-gl.txt");
            default:
                throw new IllegalCallerException();
        }
    }
}
