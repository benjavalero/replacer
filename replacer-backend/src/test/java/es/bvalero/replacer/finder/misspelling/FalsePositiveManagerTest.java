package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class FalsePositiveManagerTest {
    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private FalsePositiveManager falsePositiveManager;

    @BeforeEach
    public void setUp() {
        falsePositiveManager = new FalsePositiveManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testParseFalsePositiveListText() {
        String falsePositiveListText =
            "Text\n" +
            "\n" + // Empty line
            " \n" + // Blank line
            " # A\n" + // Commented
            "A\n" + // No starting whitespace
            " B\n" +
            " b # X\n" + // With trailing comment
            " c\n" +
            " c\n"; // Duplicated

        Collection<String> falsePositives = falsePositiveManager.parseItemsText(falsePositiveListText);
        Assertions.assertEquals(3, falsePositives.size());
        Assertions.assertTrue(falsePositives.contains("B"));
        Assertions.assertTrue(falsePositives.contains("b"));
        Assertions.assertTrue(falsePositives.contains("c"));
    }

    @Test
    void testUpdate() throws ReplacerException {
        Mockito
            .when(wikipediaService.getFalsePositiveListPageContent(Mockito.any(WikipediaLanguage.class)))
            .thenReturn("");

        falsePositiveManager.update();

        Mockito.verify(wikipediaService).getFalsePositiveListPageContent(WikipediaLanguage.SPANISH);
    }
}
