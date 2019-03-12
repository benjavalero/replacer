package es.bvalero.replacer.finder;

import es.bvalero.replacer.persistence.ReplacementType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReplacementFinderServiceTest {

    @Mock
    private List<ArticleReplacementFinder> articleReplacementFinders;

    @Mock
    private List<IgnoredReplacementFinder> ignoredReplacementFinders;

    @InjectMocks
    private ReplacementFinderService replacementFinderService;

    @Before
    public void setUp() {
        replacementFinderService = new ReplacementFinderService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindReplacementsEmpty() {
        ArticleReplacementFinder finder = Mockito.mock(ArticleReplacementFinder.class);
        Mockito.when(articleReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(finder).iterator());

        Assert.assertTrue(replacementFinderService.findReplacements("").isEmpty());
    }

    @Test
    public void testFindReplacements() {
        ArticleReplacement replacement = ArticleReplacement.builder()
                .setType(ReplacementType.MISSPELLING)
                .setSubtype("")
                .build();
        ArticleReplacementFinder finder = Mockito.mock(ArticleReplacementFinder.class);
        Mockito.when(finder.findReplacements(Mockito.anyString()))
                .thenReturn(Collections.singletonList(replacement));
        Mockito.when(articleReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(finder).iterator());
        Mockito.when(ignoredReplacementFinders.iterator()).thenReturn(Collections.<IgnoredReplacementFinder>emptyIterator());

        List<ArticleReplacement> replacements = replacementFinderService.findReplacements("");

        Assert.assertFalse(replacements.isEmpty());
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement));
    }

    @Test
    public void testFindReplacementsIgnoringExceptions() {
        ArticleReplacement replacement1 = ArticleReplacement.builder()
                .setStart(0)
                .setText("1")
                .setType(ReplacementType.MISSPELLING)
                .setSubtype("1")
                .build();
        ArticleReplacement replacement2 = ArticleReplacement.builder()
                .setStart(1)
                .setText("2")
                .setType(ReplacementType.MISSPELLING)
                .setSubtype("2")
                .build();
        ArticleReplacementFinder finder = Mockito.mock(ArticleReplacementFinder.class);
        Mockito.when(finder.findReplacements(Mockito.anyString()))
                .thenReturn(Arrays.asList(replacement1, replacement2));
        Mockito.when(articleReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(finder).iterator());

        ArticleReplacement ignored1 = ArticleReplacement.builder()
                .setStart(0)
                .setText("1")
                .setType(ReplacementType.IGNORED)
                .build();
        IgnoredReplacementFinder ignoredFinder = Mockito.mock(IgnoredReplacementFinder.class);
        Mockito.when(ignoredFinder.findIgnoredReplacements(Mockito.anyString()))
                .thenReturn(Collections.singletonList(ignored1));
        Mockito.when(ignoredReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(ignoredFinder).iterator());

        List<ArticleReplacement> replacements = replacementFinderService.findReplacements("");

        Assert.assertFalse(replacements.isEmpty());
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement2));
    }

}
