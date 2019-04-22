package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;

public interface IWikipediaFacade {

    String MISSPELLING_LIST_ARTICLE = "Wikipedia:Corrector_ortográfico/Listado";
    String FALSE_POSITIVE_LIST_ARTICLE = "Usuario:Benjavalero/FalsePositives";

    // TODO : Take into account the difference between UnavailableArticleException and the generic WikipediaException
    String getArticleContent(String articleTitle) throws WikipediaException;

    void editArticleContent(String articleTitle, String articleContent, OAuth1AccessToken accessToken) throws WikipediaException;

}
