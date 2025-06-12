package es.bvalero.replacer.page.index;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.find.WikipediaNamespace;
import es.bvalero.replacer.page.find.WikipediaTimestamp;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public interface IndexablePage {
    int SHORT_CONTENT_LENGTH = 50;

    PageKey getPageKey();

    String getTitle();

    String getContent();

    WikipediaNamespace getNamespace();

    // Store time (and not only date) in case it is needed in the future
    WikipediaTimestamp getLastUpdate();

    /* If the page is considered a redirection page */
    boolean isRedirect();

    default String getAbbreviatedContent() {
        return StringUtils.abbreviate(getContent(), SHORT_CONTENT_LENGTH);
    }

    default FinderPage toFinderPage() {
        return FinderPage.of(getPageKey(), Objects.requireNonNull(getTitle()), Objects.requireNonNull(getContent()));
    }
}
