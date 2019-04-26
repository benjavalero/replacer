package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;

import java.time.LocalDateTime;

interface IWikipediaFacade {

    String MISSPELLING_LIST_PAGE = "Wikipedia:Corrector_ortográfico/Listado";
    String FALSE_POSITIVE_LIST_PAGE = "Usuario:Benjavalero/FalsePositives";

    // TODO : Take into account the difference between UnavailablePageException and the generic WikipediaException
    String getPageContent(String pageTitle) throws WikipediaException;

    void savePageContent(String pageTitle, String pageContent, LocalDateTime editTime, OAuth1AccessToken accessToken)
            throws WikipediaException;

}
