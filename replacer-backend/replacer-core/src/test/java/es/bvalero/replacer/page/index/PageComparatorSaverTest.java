package es.bvalero.replacer.page.index;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.page.count.PageCountRepository;
import es.bvalero.replacer.replacement.ReplacementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageComparatorSaverTest {

    // Dependency injection
    private PageService pageService;
    private ReplacementService replacementService;
    private PageCountRepository pageCountRepository;

    private PageComparatorSaver pageComparatorSaver;

    @BeforeEach
    void setUp() {
        pageService = mock(PageService.class);
        replacementService = mock(ReplacementService.class);
        pageCountRepository = mock(PageCountRepository.class);
        pageComparatorSaver = new PageComparatorSaver(pageService, replacementService, pageCountRepository);
    }

    @Test
    void testSave() {
        pageComparatorSaver.save(PageComparatorResult.of(WikipediaLanguage.getDefault()));

        verify(pageService).updatePages(anyCollection());
        verify(pageService).addPages(anyCollection());
        verify(replacementService).updateReplacements(anyCollection());
        verify(replacementService).addReplacements(anyCollection());
        verify(replacementService).removeReplacements(anyCollection());
    }
}
