package es.bvalero.replacer.finder.replacement.custom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import es.bvalero.replacer.finder.CustomReplacementFindRequest;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.ImmutableFindApi;
import es.bvalero.replacer.finder.Replacement;
import java.util.SortedSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomReplacementFinderServiceTest {

    private CustomReplacementFinderService customReplacementFinderService;

    @BeforeEach
    public void setUp() {
        ImmutableFindApi immutableFindApi = mock(ImmutableFindApi.class);
        customReplacementFinderService = new CustomReplacementFinderService(immutableFindApi);
    }

    @Test
    void testFindCustomReplacements() {
        FinderPage page = FinderPage.of("A X C");
        CustomReplacementFindRequest options = CustomReplacementFindRequest.of("X", true, "Y");

        SortedSet<Replacement> replacements = customReplacementFinderService.findCustomReplacements(page, options);

        assertFalse(replacements.isEmpty());
        assertEquals(1, replacements.size());
        assertEquals("X", replacements.stream().findFirst().orElseThrow().text());
    }

    @Test
    void testFindCustomReplacementsWithNoResults() {
        FinderPage page = FinderPage.of("AXC");
        CustomReplacementFindRequest options = CustomReplacementFindRequest.of("X", true, "Y");

        SortedSet<Replacement> replacements = customReplacementFinderService.findCustomReplacements(page, options);

        assertTrue(replacements.isEmpty());
    }
}
