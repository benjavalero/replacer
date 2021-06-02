package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.FileUtils;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@VisibleForTesting
@Component
@Profile("offline")
public class ListingContentOfflineService implements ListingContentService {

    @Override
    public String getMisspellingListingContent(WikipediaLanguage lang) throws ReplacerException {
        return FileUtils.getFileContent("/offline/misspelling-list.txt");
    }

    @Override
    public String getFalsePositiveListingContent(WikipediaLanguage lang) throws ReplacerException {
        return FileUtils.getFileContent("/offline/false-positives.txt");
    }

    @Override
    public String getComposedMisspellingListingContent(WikipediaLanguage lang) throws ReplacerException {
        return FileUtils.getFileContent("/offline/composed-misspellings.txt");
    }
}
