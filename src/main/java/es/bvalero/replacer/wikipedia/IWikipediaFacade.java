package es.bvalero.replacer.wikipedia;

public interface IWikipediaFacade {

    String MISSPELLING_LIST_ARTICLE = "Wikipedia:Corrector_ortográfico/Listado";

    String getArticleContent(String articleTitle) throws Exception;

    void editArticleContent(String articleTitle, String articleContent, String editSummary);

}
