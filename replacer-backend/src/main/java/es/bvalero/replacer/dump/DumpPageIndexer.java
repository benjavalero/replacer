package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.page.index.IndexablePage;
import es.bvalero.replacer.page.index.IndexablePageMapper;
import es.bvalero.replacer.page.index.NonIndexablePageException;
import es.bvalero.replacer.page.index.PageIndexer;
import es.bvalero.replacer.page.repository.PageRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
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
    @Qualifier("pageCacheRepository")
    private PageRepository pageRepository;

    @Autowired
    private PageIndexer pageIndexer;

    @Loggable(prepend = true, value = Loggable.TRACE)
    DumpPageIndexResult index(DumpPage dumpPage) {
        WikipediaPage page = DumpPageMapper.toDomain(dumpPage);

        // In all cases we find the current status of the page in the DB
        Optional<IndexablePage> dbPage = Optional.ofNullable(
            IndexablePageMapper.fromModel(pageRepository.findByPageId(page.getId()).orElse(null))
        );

        // Find the replacements to index and index the page
        try {
            return indexPage(page, dbPage.orElse(null));
        } catch (Exception e) {
            // Just in case capture possible exceptions to continue indexing other pages
            LOGGER.error("Page not indexed: {}", dumpPage, e);
            return DumpPageIndexResult.PAGE_NOT_INDEXED;
        }
    }

    private DumpPageIndexResult indexPage(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // Index the found replacements against the ones in DB (if any)
        boolean pageIndexed;
        try {
            pageIndexed = pageIndexer.indexPageReplacements(page, dbPage);
        } catch (NonIndexablePageException e) {
            // If the page is not indexable then it should not exist in DB
            if (dbPage != null) {
                LOGGER.error(
                    "Unexpected page in DB not indexable: {} - {} - {}",
                    page.getId().getLang(),
                    page.getTitle(),
                    dbPage.getTitle()
                );
            }
            return DumpPageIndexResult.PAGE_NOT_INDEXABLE;
        }

        return pageIndexed ? DumpPageIndexResult.PAGE_INDEXED : DumpPageIndexResult.PAGE_NOT_INDEXED;
    }

    void finish() {
        pageIndexer.forceSave();
        pageRepository.resetCache();
    }
}
