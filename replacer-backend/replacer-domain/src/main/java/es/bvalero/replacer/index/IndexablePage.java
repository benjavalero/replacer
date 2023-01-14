package es.bvalero.replacer.index;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.time.LocalDateTime;

public interface IndexablePage extends FinderPage {
    WikipediaNamespace getNamespace();

    LocalDateTime getLastUpdate();

    boolean isRedirect();
}
