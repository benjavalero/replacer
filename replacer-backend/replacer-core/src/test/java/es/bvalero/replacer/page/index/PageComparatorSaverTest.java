package es.bvalero.replacer.page.index;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.page.count.PageCountRepository;
import es.bvalero.replacer.replacement.ReplacementSaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageComparatorSaverTest {

    // Dependency injection
    private PageService pageService;
    private PageCountRepository pageCountRepository;
    private ReplacementSaveRepository replacementSaveRepository;

    private PageComparatorSaver pageComparatorSaver;

    @BeforeEach
    void setUp() {
        pageService = mock(PageService.class);
        pageCountRepository = mock(PageCountRepository.class);
        replacementSaveRepository = mock(ReplacementSaveRepository.class);
        pageComparatorSaver = new PageComparatorSaver(pageService, pageCountRepository, replacementSaveRepository);
    }

    @Test
    void testSave() {
        pageComparatorSaver.save(PageComparatorResult.of(WikipediaLanguage.getDefault()));

        verify(pageService).updatePages(anyCollection());
        verify(pageService).addPages(anyCollection());
        verify(replacementSaveRepository).update(anyCollection());
        verify(replacementSaveRepository).add(anyCollection());
        verify(replacementSaveRepository).remove(anyCollection());
    }
}
