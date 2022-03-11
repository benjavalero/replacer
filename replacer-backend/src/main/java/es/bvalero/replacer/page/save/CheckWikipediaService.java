package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.CheckWikipediaAction;
import es.bvalero.replacer.common.domain.WikipediaLanguage;

/**
 * Report to Check-Wikipedia service (https://checkwiki.toolforge.org/)
 * that a page fix has been done by Replacer.
 */
interface CheckWikipediaService {
    void reportFix(WikipediaLanguage lang, String pageTitle, CheckWikipediaAction action);
}
