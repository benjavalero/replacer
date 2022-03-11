package es.bvalero.replacer.finder.replacement.custom;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.common.domain.Replacement;
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
        FinderPage page = FinderPage.of("A X C");

        List<Replacement> replacements = IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(page, CustomOptions.of("X", true, "Y"))
        );

        assertFalse(replacements.isEmpty());
        assertEquals(1, replacements.size());
        assertEquals("X", replacements.get(0).getText());
    }

    @Test
    void testFindCustomReplacementsWithNoResults() {
        FinderPage page = FinderPage.of("AXC");

        List<Replacement> replacements = IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(page, CustomOptions.of("X", true, "Y"))
        );

        assertTrue(replacements.isEmpty());
    }
}
