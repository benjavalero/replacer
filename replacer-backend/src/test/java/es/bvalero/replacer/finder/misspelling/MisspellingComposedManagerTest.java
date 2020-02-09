package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MisspellingComposedManagerTest {

    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private MisspellingComposedManager misspellingComposedManager;

    @Before
    public void setUp() {
        misspellingComposedManager = new MisspellingComposedManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUpdate() throws WikipediaException {
        Mockito.when(wikipediaService.getComposedMisspellingListPageContent()).thenReturn("");

        misspellingComposedManager.update();

        Mockito.verify(wikipediaService).getComposedMisspellingListPageContent();
    }

}
