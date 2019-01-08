package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.oauth.OAuth10aService;

public interface IWikipediaFacade {

    String TOKEN_REQUEST = "requestToken";
    String TOKEN_ACCESS = "accessToken";
    String TOKEN_VERIFIER = "oauth_verifier";
    String EDIT_SUMMARY = "Correcciones ortográficas";
    String MISSPELLING_LIST_ARTICLE = "Wikipedia:Corrector_ortográfico/Listado";
    String FALSE_POSITIVE_LIST_ARTICLE = "Usuario:Benjavalero/FalsePositives";

    OAuth10aService getOAuthService();

    String getArticleContent(String articleTitle) throws WikipediaException;

    void editArticleContent(String articleTitle, String articleContent) throws WikipediaException;

}
