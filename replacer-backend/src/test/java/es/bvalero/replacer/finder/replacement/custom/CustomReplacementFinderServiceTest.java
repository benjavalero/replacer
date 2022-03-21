package es.bvalero.replacer.finder.replacement.custom;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.ReviewOptions;
import es.bvalero.replacer.common.domain.WikipediaPage;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

class CustomReplacementFinderServiceTest {

    @InjectMocks
    private CustomReplacementFinderService customReplacementFinderService;

    @BeforeEach
    public void setUp() {
        customReplacementFinderService = new CustomReplacementFinderService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindCustomReplacements() {
        WikipediaPage page = WikipediaPage.of("A X C");
        ReviewOptions options = ReviewOptions.ofCustom(page.getId().getLang(), "X", "Y", true);

        List<Replacement> replacements = IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(page, options)
        );

        assertFalse(replacements.isEmpty());
        assertEquals(1, replacements.size());
        assertEquals("X", replacements.get(0).getText());
    }

    @Test
    void testFindCustomReplacementsWithNoResults() {
        WikipediaPage page = WikipediaPage.of("AXC");
        ReviewOptions options = ReviewOptions.ofCustom(page.getId().getLang(), "X", "Y", true);

        List<Replacement> replacements = IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(page, options)
        );

        assertTrue(replacements.isEmpty());
    }
}
