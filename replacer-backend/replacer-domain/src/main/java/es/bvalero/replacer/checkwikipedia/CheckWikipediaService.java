package es.bvalero.replacer.checkwikipedia;

import es.bvalero.replacer.common.domain.WikipediaLanguage;

/**
 * Report to Check-Wikipedia service (https://checkwiki.toolforge.org/)
 * that a page fix has been done by Replacer.
 */
public interface CheckWikipediaService {
    void reportFix(WikipediaLanguage lang, String pageTitle, CheckWikipediaAction action);
}