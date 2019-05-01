package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class WikipediaService {

    private static final String REDIRECT_PREFIX = "#redirec";

    @Autowired
    private IWikipediaFacade wikipediaFacade;

    public String getPageContent(String pageTitle) throws WikipediaException {
        return wikipediaFacade.getPageContent(pageTitle);
    }

    @Deprecated
    public String getPageContent(String pageTitle, OAuth1AccessToken accessToken) throws WikipediaException {
        return wikipediaFacade.getPageContent(pageTitle, accessToken);
    }

    public String getPageContent(int pageId, OAuth1AccessToken accessToken) throws WikipediaException {
        return wikipediaFacade.getPageContent(pageId, accessToken);
    }

    public Map<Integer, String> getPagesContent(List<Integer> pageIds, OAuth1AccessToken accessToken)
            throws WikipediaException {
        return wikipediaFacade.getPagesContent(pageIds, accessToken);
    }

    public void savePageContent(String pageTitle, String pageContent, LocalDateTime editTime, OAuth1AccessToken accessToken)
            throws WikipediaException {
        wikipediaFacade.savePageContent(pageTitle, pageContent, editTime, accessToken);
    }

    public boolean isRedirectionPage(String pageContent) {
        return pageContent.toLowerCase().contains(REDIRECT_PREFIX);
    }

    public String getMisspellingListPageContent() throws WikipediaException {
        return getPageContent(IWikipediaFacade.MISSPELLING_LIST_PAGE);
    }

    public String getFalsePositiveListPageContent() throws WikipediaException {
        return getPageContent(IWikipediaFacade.FALSE_POSITIVE_LIST_PAGE);
    }

}
