package es.bvalero.replacer.page.index;

import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.verify;

import es.bvalero.replacer.page.repository.PageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageIndexResultSaverTest {

    @Mock
    private PageRepository pageRepository;

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

        verify(pageRepository).updatePageTitles(anyCollection());
        verify(pageRepository).insertPages(anyCollection());
        verify(pageRepository).updateReplacements(anyCollection());
        verify(pageRepository).insertReplacements(anyCollection());
        verify(pageRepository).deleteReplacements(anyCollection());
    }
}
