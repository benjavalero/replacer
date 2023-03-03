package es.bvalero.replacer.finder.replacement.custom;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
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
        CustomType options = CustomType.of("X", true, "Y");

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
        CustomType options = CustomType.of("X", true, "Y");

        List<Replacement> replacements = IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(page, options)
        );

        assertTrue(replacements.isEmpty());
    }
}
