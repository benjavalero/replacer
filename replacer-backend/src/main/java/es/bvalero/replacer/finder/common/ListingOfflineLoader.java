package es.bvalero.replacer.finder.common;

import es.bvalero.replacer.common.FileUtils;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("offline")
class ListingOfflineLoader implements ListingLoader {

    @Override
    public String getMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return FileUtils.getFileContent("/offline/misspelling-list.txt");
    }

    @Override
    public String getFalsePositiveListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return FileUtils.getFileContent("/offline/false-positives.txt");
    }

    @Override
    public String getComposedMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return FileUtils.getFileContent("/offline/composed-misspellings.txt");
    }
}
