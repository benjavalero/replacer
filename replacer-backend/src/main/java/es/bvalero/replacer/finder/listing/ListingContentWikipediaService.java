package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!offline")
class ListingContentWikipediaService implements ListingContentService {

    @Autowired
    private WikipediaService wikipediaService;

    @Resource
    private Map<String, String> simpleMisspellingPages;

    @Resource
    private Map<String, String> composedMisspellingPages;

    @Resource
    private Map<String, String> falsePositivePages;

    @Override
    public String getMisspellingListingContent(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaService
            .getPageByTitle(lang, simpleMisspellingPages.get(lang.getCode()))
            .orElseThrow(ReplacerException::new)
            .getContent();
    }

    @Override
    public String getFalsePositiveListingContent(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaService
            .getPageByTitle(lang, falsePositivePages.get(lang.getCode()))
            .orElseThrow(ReplacerException::new)
            .getContent();
    }

    @Override
    public String getComposedMisspellingListingContent(WikipediaLanguage lang) throws ReplacerException {
        return wikipediaService
            .getPageByTitle(lang, composedMisspellingPages.get(lang.getCode()))
            .orElseThrow(ReplacerException::new)
            .getContent();
    }
}
