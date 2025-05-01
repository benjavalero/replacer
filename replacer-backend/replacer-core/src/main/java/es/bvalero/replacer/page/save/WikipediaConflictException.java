package es.bvalero.replacer.page.save;

import es.bvalero.replacer.wikipedia.WikipediaException;
import lombok.experimental.StandardException;

/**
 * An exception thrown when saving a page on Wikipedia,
 * in case there is an edit conflict so the saving cannot be completed.
 */
@StandardException
public class WikipediaConflictException extends WikipediaException {}
