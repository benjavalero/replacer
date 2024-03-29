package es.bvalero.replacer.page.index;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.page.find.WikipediaNamespace;
import es.bvalero.replacer.page.find.WikipediaTimestamp;

public interface IndexablePage extends FinderPage {
    WikipediaNamespace getNamespace();

    // Store time (and not only date) in case it is needed in the future
    WikipediaTimestamp getLastUpdate();

    /* If the page is considered a redirection page */
    boolean isRedirect();
}
