package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageIndexValidatorTest {

    private PageIndexValidator pageIndexValidator;

    @BeforeEach
    void setUp() {
        this.pageIndexValidator = new PageIndexValidator();
        this.pageIndexValidator.setIndexableNamespaces(Set.of(0, 104));
        this.pageIndexValidator.init();
    }

    @Test
    void testIsPageIndexableByNamespace() {
        WikipediaPage notIndexable = buildWikipediaPage(WikipediaNamespace.WIKIPEDIA);
        WikipediaPage articlePage = buildWikipediaPage(WikipediaNamespace.ARTICLE);
        WikipediaPage annexPage = buildWikipediaPage(WikipediaNamespace.ANNEX);

        assertFalse(pageIndexValidator.isPageIndexableByNamespace(notIndexable));
        assertTrue(pageIndexValidator.isPageIndexableByNamespace(articlePage));
        assertTrue(pageIndexValidator.isPageIndexableByNamespace(annexPage));
    }

    private WikipediaPage buildWikipediaPage(WikipediaNamespace namespace) {
        return WikipediaPage.builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
            .namespace(namespace)
            .title("T")
            .content("")
            .lastUpdate(WikipediaTimestamp.now())
            .queryTimestamp(WikipediaTimestamp.now())
            .build();
    }

    @Test
    void testIsPageNotIndexableByTimestamp() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1);

        WikipediaPage page1 = buildWikipediaPage(today);
        assertTrue(pageIndexValidator.isIndexableByTimestamp(page1, null));

        WikipediaPage page2 = buildWikipediaPage(today);
        IndexedPage dbPage2 = buildIndexedPage(today);
        assertTrue(pageIndexValidator.isIndexableByTimestamp(page2, dbPage2));

        WikipediaPage page3 = buildWikipediaPage(yesterday);
        IndexedPage dbPage3 = buildIndexedPage(today);
        assertFalse(pageIndexValidator.isIndexableByTimestamp(page3, dbPage3));

        WikipediaPage page4 = buildWikipediaPage(today);
        IndexedPage dbPage4 = buildIndexedPage(yesterday);
        assertTrue(pageIndexValidator.isIndexableByTimestamp(page4, dbPage4));
    }

    private WikipediaPage buildWikipediaPage(LocalDateTime lastUpdate) {
        return WikipediaPage.builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
            .namespace(WikipediaNamespace.ARTICLE)
            .title("T")
            .content("")
            .lastUpdate(WikipediaTimestamp.of(lastUpdate))
            .queryTimestamp(WikipediaTimestamp.now())
            .build();
    }

    private IndexedPage buildIndexedPage(LocalDateTime lastUpdate) {
        return IndexedPage.builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
            .title("T")
            .lastUpdate(lastUpdate.toLocalDate())
            .build();
    }
}
