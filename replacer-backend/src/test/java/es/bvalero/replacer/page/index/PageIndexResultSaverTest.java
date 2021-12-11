package es.bvalero.replacer.page.index;

import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.verify;

import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.repository.ReplacementRepository;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageIndexResultSaverTest {

    @Mock
    private PageRepository pageRepository;

    @Mock
    private ReplacementRepository replacementRepository;

    @InjectMocks
    private PageIndexResultSaver pageIndexResultSaver;

    @BeforeEach
    void setUp() {
        pageIndexResultSaver = new PageIndexResultSaver();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave() {
        pageIndexResultSaver.save(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, Collections.emptyList()));

        verify(pageRepository).updatePages(anyCollection());
        verify(pageRepository).addPages(anyCollection());
        verify(replacementRepository).updateReplacements(anyCollection());
        verify(replacementRepository).addReplacements(anyCollection());
        verify(replacementRepository).removeReplacements(anyCollection());
    }
}
