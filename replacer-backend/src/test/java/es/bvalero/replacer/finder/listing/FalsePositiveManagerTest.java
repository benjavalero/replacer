package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
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
    private ListingFinder listingFinder;

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
        Mockito.when(listingFinder.getFalsePositiveListing(Mockito.any(WikipediaLanguage.class))).thenReturn("");

        falsePositiveManager.scheduledItemListUpdate();

        Mockito.verify(listingFinder).getFalsePositiveListing(WikipediaLanguage.SPANISH);
    }
}
