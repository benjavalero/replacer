package es.bvalero.replacer.page.index;

import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.verify;

import es.bvalero.replacer.page.repository.IndexablePageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageIndexResultSaverTest {

    @Mock
    private IndexablePageRepository indexablePageRepository;

    @InjectMocks
    private PageIndexResultSaver pageIndexResultSaver;

    @BeforeEach
    void setUp() {
        pageIndexResultSaver = new PageIndexResultSaver();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testSave() {
        pageIndexResultSaver.save(PageIndexResult.ofEmpty());

        verify(indexablePageRepository).updatePageTitles(anyCollection());
        verify(indexablePageRepository).insertPages(anyCollection());
        verify(indexablePageRepository).updateReplacements(anyCollection());
        verify(indexablePageRepository).insertReplacements(anyCollection());
        verify(indexablePageRepository).deleteReplacements(anyCollection());
    }
}
