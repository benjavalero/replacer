package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WikipediaService {

    private static final String TAG_REDIRECTION = "#REDIRECCIÓN";
    private static final String TAG_REDIRECT = "#REDIRECT";

    @Autowired
    private IWikipediaFacade wikipediaFacade;

    public String getPageContent(String pageTitle) throws WikipediaException {
        return getPageContent(pageTitle, null);
    }

    // TODO : To be refactored to receive a pageId instead of a title
    public String getPageContent(String pageTitle, OAuth1AccessToken accessToken) throws WikipediaException {
        return wikipediaFacade.getPageContent(pageTitle, accessToken);
    }

    public void savePageContent(String pageTitle, String pageContent, LocalDateTime editTime, OAuth1AccessToken accessToken)
            throws WikipediaException {
        wikipediaFacade.savePageContent(pageTitle, pageContent, editTime, accessToken);
    }

    public boolean isRedirectionPage(String pageContent) {
        return StringUtils.containsIgnoreCase(pageContent, TAG_REDIRECTION)
                || StringUtils.containsIgnoreCase(pageContent, TAG_REDIRECT);
    }

    public String getMisspellingListPageContent() throws WikipediaException {
        return getPageContent(IWikipediaFacade.MISSPELLING_LIST_PAGE);
    }

    public String getFalsePositiveListPageContent() throws WikipediaException {
        return getPageContent(IWikipediaFacade.FALSE_POSITIVE_LIST_PAGE);
    }

}
