package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class FalsePositiveManagerTest {
    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private FalsePositiveManager falsePositiveManager;

    @Before
    public void setUp() {
        falsePositiveManager = new FalsePositiveManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testParseFalsePositiveListText() {
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
        Assert.assertEquals(3, falsePositives.size());
        Assert.assertTrue(falsePositives.contains("B"));
        Assert.assertTrue(falsePositives.contains("b"));
        Assert.assertTrue(falsePositives.contains("c"));
    }

    @Test
    public void testUpdate() throws WikipediaException {
        Mockito.when(wikipediaService.getFalsePositiveListPageContent()).thenReturn("");

        falsePositiveManager.update();

        Mockito.verify(wikipediaService).getFalsePositiveListPageContent();
    }
}
