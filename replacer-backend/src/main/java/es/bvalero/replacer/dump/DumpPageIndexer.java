package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.page.index.PageIndexStatus;
import es.bvalero.replacer.page.index.PageIndexer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Index a page found in a Wikipedia dump */
@Slf4j
@Component
class DumpPageIndexer {

    @Autowired
    @Qualifier("pageBatchIndexer")
    private PageIndexer pageIndexer;

    @Loggable(prepend = true, value = Loggable.TRACE)
    PageIndexStatus index(DumpPage dumpPage) {
        WikipediaPage page = DumpPageMapper.toDomain(dumpPage);
        return pageIndexer.indexPageReplacements(page).getStatus();
    }

    void finish() {
        pageIndexer.forceSave();
    }
}
