package es.bvalero.replacer.finder.replacement.custom;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.Replacement;
import java.util.List;

import es.bvalero.replacer.page.review.PageReviewOptions;
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
        WikipediaPage page = WikipediaPage.ofContent("A X C");
        PageReviewOptions options = PageReviewOptions.ofCustom(
            page.getId().getLang(),
            "X", "Y", true
        );

        List<Replacement> replacements = IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(page, options)
        );

        assertFalse(replacements.isEmpty());
        assertEquals(1, replacements.size());
        assertEquals("X", replacements.get(0).getText());
    }

    @Test
    void testFindCustomReplacementsWithNoResults() {
        WikipediaPage page = WikipediaPage.ofContent("AXC");
        PageReviewOptions options = PageReviewOptions.ofCustom(
            page.getId().getLang(),
            "X", "Y", true
        );

        List<Replacement> replacements = IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(page, options)
        );

        assertTrue(replacements.isEmpty());
    }
}
