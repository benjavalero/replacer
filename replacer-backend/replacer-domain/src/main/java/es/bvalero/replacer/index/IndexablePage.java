package es.bvalero.replacer.index;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;

public interface IndexablePage extends FinderPage {
    WikipediaNamespace getNamespace();

    WikipediaTimestamp getLastUpdate();

    boolean isRedirect();
}
