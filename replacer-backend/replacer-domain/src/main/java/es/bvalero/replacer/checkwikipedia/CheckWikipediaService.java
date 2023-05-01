package es.bvalero.replacer.checkwikipedia;

import es.bvalero.replacer.common.domain.WikipediaLanguage;

/**
 * Report to <a href="https://checkwiki.toolforge.org/">Check-Wikipedia</a>
 * that a page fix has been done by Replacer.
 */
public interface CheckWikipediaService {
    void reportFix(WikipediaLanguage lang, String pageTitle, CheckWikipediaAction action);
}
