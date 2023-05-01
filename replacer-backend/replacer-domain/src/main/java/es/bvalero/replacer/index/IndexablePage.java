package es.bvalero.replacer.index;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;

public interface IndexablePage extends FinderPage {
    WikipediaNamespace getNamespace();

    // Store time (and not only date) in case it is needed in the future
    WikipediaTimestamp getLastUpdate();

    /* If the page is considered a redirection page */
    boolean isRedirect();
}
