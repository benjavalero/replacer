package es.bvalero.replacer.page.index;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.save.PageSaveRepository;
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
        pageComparatorSaver.save(PageComparatorResult.of(WikipediaLanguage.getDefault()));

        verify(pageSaveRepository).save(anyCollection());
    }
}
