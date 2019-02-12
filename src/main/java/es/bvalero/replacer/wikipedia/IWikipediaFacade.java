package es.bvalero.replacer.wikipedia;

public interface IWikipediaFacade {

    String MISSPELLING_LIST_ARTICLE = "Wikipedia:Corrector_ortográfico/Listado";
    String FALSE_POSITIVE_LIST_ARTICLE = "Usuario:Benjavalero/FalsePositives";

    // TODO : Take into account the difference between UnavailableArticleException and the generic WikipediaException
    String getArticleContent(String articleTitle) throws WikipediaException;

    void editArticleContent(String articleTitle, String articleContent) throws WikipediaException;

}
