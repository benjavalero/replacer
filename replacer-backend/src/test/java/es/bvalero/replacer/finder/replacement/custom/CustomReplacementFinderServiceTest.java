package es.bvalero.replacer.finder.replacement.custom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
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
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindCustomReplacements() {
        WikipediaPage page = mock(WikipediaPage.class);
        when(page.getId()).thenReturn(WikipediaPageId.of(WikipediaLanguage.getDefault(), 1));
        when(page.getContent()).thenReturn("A X C");

        List<Replacement> replacements = IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(page, CustomOptions.of("X", true, "Y"))
        );

        assertFalse(replacements.isEmpty());
        assertEquals(1, replacements.size());
        assertEquals("X", replacements.get(0).getText());
    }

    @Test
    void testFindCustomReplacementsWithNoResults() {
        WikipediaPage page = mock(WikipediaPage.class);
        when(page.getId()).thenReturn(WikipediaPageId.of(WikipediaLanguage.getDefault(), 1));
        when(page.getContent()).thenReturn("AXC");

        List<Replacement> replacements = IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(page, CustomOptions.of("X", true, "Y"))
        );

        assertTrue(replacements.isEmpty());
    }
}
