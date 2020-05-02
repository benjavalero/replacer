package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MisspellingComposedManagerTest {
    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private MisspellingComposedManager misspellingComposedManager;

    @BeforeEach
    public void setUp() {
        misspellingComposedManager = new MisspellingComposedManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUpdate() throws ReplacerException {
        Mockito
            .when(wikipediaService.getComposedMisspellingListPageContent(Mockito.any(WikipediaLanguage.class)))
            .thenReturn("");

        misspellingComposedManager.update();

        Mockito.verify(wikipediaService).getComposedMisspellingListPageContent(WikipediaLanguage.SPANISH);
    }
}
