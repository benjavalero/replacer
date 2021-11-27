package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.page.index.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Index a page found in a Wikipedia dump.
 * It involves checking if the page needs to be indexed, finding the replacements in the page text
 * and finally index the replacements in the repository.
 */
@Slf4j
@Component
class DumpPageIndexer {

    @Autowired
    @Qualifier("pageBatchIndexer")
    private PageIndexer pageIndexer;

    @Loggable(prepend = true, value = Loggable.TRACE)
    PageIndexStatus index(DumpPage dumpPage) {
        WikipediaPage page = DumpPageMapper.toDomain(dumpPage);

        // Find the replacements to index and index the page
        try {
            return pageIndexer.indexPageReplacements(page).getStatus();
        } catch (Exception e) {
            // Just in case capture possible exceptions to continue indexing other pages
            LOGGER.error("Page not indexed: {}", dumpPage, e);
            return PageIndexStatus.PAGE_NOT_INDEXED;
        }
    }

    void finish() {
        pageIndexer.forceSave();
    }
}
