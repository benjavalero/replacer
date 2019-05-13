package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WikipediaService {

    /**
     * @param pageTitle the title of the page to retrieve
     * @return The page with the given title in case it exists
     * @throws WikipediaException in case of issues performing the request
     */
    Optional<WikipediaPage> getPageByTitle(String pageTitle) throws WikipediaException;

    /**
     * @param pageIds The list of numeric IDs of the Wikipedia pages to retrieve
     * @return A map with the each and the retrieved page. If any page is missing or deleted will not exist in the map.
     * @throws WikipediaException in case of issues performing the request
     */
    Map<Integer, WikipediaPage> getPagesByIds(List<Integer> pageIds) throws WikipediaException;

    void savePageContent(String pageTitle, String pageContent, LocalDateTime editTime, OAuth1AccessToken accessToken)
            throws WikipediaException;

    String getMisspellingListPageContent() throws WikipediaException;

    String getFalsePositiveListPageContent() throws WikipediaException;

}
