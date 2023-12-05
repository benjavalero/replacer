package es.bvalero.replacer.finder.replacement.custom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import es.bvalero.replacer.finder.CustomMisspelling;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomReplacementFinderServiceTest {

    private CustomReplacementFinderService customReplacementFinderService;

    @BeforeEach
    public void setUp() {
        ReplacementFinder replacementFinder = mock(ReplacementFinder.class);
        ImmutableFinderService immutableFinderService = mock(ImmutableFinderService.class);
        customReplacementFinderService =
            new CustomReplacementFinderService(List.of(replacementFinder), immutableFinderService);
    }

    @Test
    void testFindCustomReplacements() {
        FinderPage page = FinderPage.of("A X C");
        CustomMisspelling options = CustomMisspelling.of("X", true, "Y");

        List<Replacement> replacements = IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(page, options)
        );

        assertFalse(replacements.isEmpty());
        assertEquals(1, replacements.size());
        assertEquals("X", replacements.get(0).getText());
    }

    @Test
    void testFindCustomReplacementsWithNoResults() {
        FinderPage page = FinderPage.of("AXC");
        CustomMisspelling options = CustomMisspelling.of("X", true, "Y");

        List<Replacement> replacements = IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(page, options)
        );

        assertTrue(replacements.isEmpty());
    }
}
