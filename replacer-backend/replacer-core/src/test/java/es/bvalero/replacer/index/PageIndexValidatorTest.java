package es.bvalero.replacer.index;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        IndexablePage notIndexable = buildIndexablePage(WikipediaNamespace.WIKIPEDIA);
        IndexablePage articlePage = buildIndexablePage(WikipediaNamespace.ARTICLE);
        IndexablePage annexPage = buildIndexablePage(WikipediaNamespace.ANNEX);

        assertFalse(pageIndexValidator.isPageIndexableByNamespace(notIndexable));
        assertTrue(pageIndexValidator.isPageIndexableByNamespace(articlePage));
        assertTrue(pageIndexValidator.isPageIndexableByNamespace(annexPage));
    }

    private IndexablePage buildIndexablePage(WikipediaNamespace namespace) {
        return IndexablePage.builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
            .namespace(namespace.getValue())
            .title("T")
            .content("")
            .lastUpdate(WikipediaTimestamp.now().toString())
            .build();
    }

    @Test
    void testIsPageNotIndexableByTimestamp() {
        LocalDateTime today = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime yesterday = today.minusDays(1);

        IndexablePage page1 = buildIndexablePage(today);
        assertTrue(pageIndexValidator.isIndexableByTimestamp(page1, null));

        IndexablePage page2 = buildIndexablePage(today);
        IndexedPage dbPage2 = buildIndexedPage(today);
        assertTrue(pageIndexValidator.isIndexableByTimestamp(page2, dbPage2));

        IndexablePage page3 = buildIndexablePage(yesterday);
        IndexedPage dbPage3 = buildIndexedPage(today);
        assertFalse(pageIndexValidator.isIndexableByTimestamp(page3, dbPage3));

        IndexablePage page4 = buildIndexablePage(today);
        IndexedPage dbPage4 = buildIndexedPage(yesterday);
        assertTrue(pageIndexValidator.isIndexableByTimestamp(page4, dbPage4));
    }

    private IndexablePage buildIndexablePage(LocalDateTime lastUpdate) {
        return IndexablePage.builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
            .namespace(WikipediaNamespace.getDefault().getValue())
            .title("T")
            .content("")
            .lastUpdate(WikipediaTimestamp.of(lastUpdate).toString())
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
