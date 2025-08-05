package es.bvalero.replacer.page.index;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.save.PageSaveRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageComparatorSaverTest {

    // Dependency injection
    private PageSaveRepository pageSaveRepository;

    private PageComparatorSaver pageComparatorSaver;

    @BeforeEach
    void setUp() {
        pageSaveRepository = mock(PageSaveRepository.class);
        pageComparatorSaver = new PageComparatorSaver(pageSaveRepository);
    }

    @Test
    void testSave() {
        IndexedPage page = IndexedPage.builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
            .title("T")
            .lastUpdate(LocalDate.now())
            .build();

        pageComparatorSaver.save(page);

        verify(pageSaveRepository).save(anyCollection());
    }
}
