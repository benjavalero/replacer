package es.bvalero.replacer.index;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;

public interface IndexablePage extends FinderPage {
    int SHORT_CONTENT_LENGTH = 50;

    WikipediaNamespace getNamespace();

    WikipediaTimestamp getLastUpdate();

    boolean isRedirect();
}
