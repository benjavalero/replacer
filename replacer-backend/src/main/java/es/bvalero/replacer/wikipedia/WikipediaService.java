package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WikipediaService {

    String getLoggedUserName(OAuth1AccessToken accessToken) throws WikipediaException;

    boolean isAdminUser(String username);

    String getMisspellingListPageContent() throws WikipediaException;

    String getFalsePositiveListPageContent() throws WikipediaException;

    String getComposedMisspellingListPageContent() throws WikipediaException;

    Optional<WikipediaPage> getPageByTitle(String pageTitle) throws WikipediaException;

    Optional<WikipediaPage> getPageById(int pageId) throws WikipediaException;

    List<WikipediaPage> getPagesByIds(List<Integer> pageIds) throws WikipediaException;

    List<WikipediaSection> getPageSections(int pageId) throws WikipediaException;

    Optional<WikipediaPage> getPageByIdAndSection(int pageId, int section) throws WikipediaException;

    Set<Integer> getPageIdsByStringMatch(String text) throws WikipediaException;

    void savePageContent(int pageId, String pageContent, @Nullable Integer section, String currentTimestamp,
                         OAuth1AccessToken accessToken) throws WikipediaException;

}
