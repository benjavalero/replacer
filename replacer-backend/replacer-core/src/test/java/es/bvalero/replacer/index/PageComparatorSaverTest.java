package es.bvalero.replacer.index;

import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.verify;

import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.replacement.ReplacementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageComparatorSaverTest {

    @Mock
    private PageService pageService;

    @Mock
    private ReplacementService replacementService;

    @InjectMocks
    private PageComparatorSaver pageComparatorSaver;

    @BeforeEach
    void setUp() {
        pageComparatorSaver = new PageComparatorSaver();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave() {
        pageComparatorSaver.save(PageComparatorResult.of());

        verify(pageService).updatePages(anyCollection());
        verify(pageService).addPages(anyCollection());
        verify(replacementService).updateReplacements(anyCollection());
        verify(replacementService).addReplacements(anyCollection());
        verify(replacementService).removeReplacements(anyCollection());
    }
}
