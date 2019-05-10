package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface WikipediaService {

    // TODO : Take into account the difference between UnavailablePageException and the generic WikipediaException
    String getPageContent(String pageTitle) throws WikipediaException;

    String getPageContent(String pageTitle, OAuth1AccessToken accessToken) throws WikipediaException;

    Map<Integer, String> getPagesContent(List<Integer> pageIds, OAuth1AccessToken accessToken) throws WikipediaException;

    void savePageContent(String pageTitle, String pageContent, LocalDateTime editTime, OAuth1AccessToken accessToken)
            throws WikipediaException;

    String getMisspellingListPageContent() throws WikipediaException;

    String getFalsePositiveListPageContent() throws WikipediaException;

}
