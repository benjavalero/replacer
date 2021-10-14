package es.bvalero.replacer.finder.replacement.custom;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import es.bvalero.replacer.finder.replacement.Replacement;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CustomReplacementFinderServiceTest {

    @Mock
    private ImmutableFinderService immutableFinderService;

    @InjectMocks
    private CustomReplacementFinderService customReplacementFinderService;

    @BeforeEach
    public void setUp() {
        customReplacementFinderService = new CustomReplacementFinderService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindCustomReplacements() {
        List<Replacement> replacements = IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(
                FinderPage.of("A X C"),
                CustomOptions.of("X", true, "Y")
            )
        );

        assertFalse(replacements.isEmpty());
        assertEquals(1, replacements.size());
        assertEquals("X", replacements.get(0).getText());
    }

    @Test
    void testFindCustomReplacementsWithNoResults() {
        List<Replacement> replacements = IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(
                FinderPage.of("AXC"),
                CustomOptions.of("X", true, "Y")
            )
        );

        assertTrue(replacements.isEmpty());
    }
}
