package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.article.Replacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class DumpArticleProcessorTest {

    @Mock
    private ArticleService articleService;

    @Mock
    private ReplacementFinderService replacementFinderService;

    @InjectMocks
    private DumpArticleProcessor dumpArticleProcessor;

    @Before
    public void setUp() {
        dumpArticleProcessor = new DumpArticleProcessor();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessSimple() {
        WikipediaPage dumpArticle = WikipediaPage.builder().setNamespace(WikipediaNamespace.ARTICLE).setContent("").build();
        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testCheckNamespaces() {
        WikipediaPage dumpArticle = WikipediaPage.builder().setNamespace(WikipediaNamespace.ARTICLE).setContent("").build();
        WikipediaPage dumpAnnex = WikipediaPage.builder().setNamespace(WikipediaNamespace.ANNEX).setContent("").build();
        WikipediaPage dumpCategory = WikipediaPage.builder().setNamespace(WikipediaNamespace.CATEGORY).build();

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpAnnex));
        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpCategory));
    }

    @Test
    public void testProcessRedirection() {
        WikipediaPage dumpArticle = WikipediaPage.builder().setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("#Redirect").build();
        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateAfterTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(yesterday)
                .build();

        Replacement dbReplacement = new Replacement(1, "", "", 0).withLastUpdate(today);
        Map<Integer, Collection<Replacement>> replacementMap = new TreeMap<>();
        replacementMap.put(1, Collections.singleton(dbReplacement));
        Mockito.when(articleService.buildReplacementMapByArticle(Mockito.anyCollection())).thenReturn(replacementMap);

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateAfterTimestampForced() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(yesterday)
                .build();

        Replacement dbReplacement = new Replacement(1, "", "", 0).withLastUpdate(today);
        Map<Integer, Collection<Replacement>> replacementMap = new TreeMap<>();
        replacementMap.put(1, Collections.singleton(dbReplacement));
        Mockito.when(articleService.buildReplacementMapByArticle(Mockito.anyCollection())).thenReturn(replacementMap);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, true));
        Mockito.verify(articleService).indexReplacements(Mockito.anyCollection());
    }

    @Test
    public void testProcessLastUpdateWhenTimestamp() {
        LocalDate today = LocalDate.now();
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        Replacement dbReplacement = new Replacement(1, "", "", 0).withLastUpdate(today);
        Map<Integer, Collection<Replacement>> replacementMap = new TreeMap<>();
        replacementMap.put(1, Collections.singleton(dbReplacement));
        Mockito.when(articleService.buildReplacementMapByArticle(Mockito.anyCollection())).thenReturn(replacementMap);

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateWhenTimestampForced() {
        LocalDate today = LocalDate.now();
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        Replacement dbReplacement = new Replacement(1, "", "", 0).withLastUpdate(today);
        Map<Integer, Collection<Replacement>> replacementMap = new TreeMap<>();
        replacementMap.put(1, Collections.singleton(dbReplacement));
        Mockito.when(articleService.buildReplacementMapByArticle(Mockito.anyCollection())).thenReturn(replacementMap);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, true));
        Mockito.verify(articleService).indexReplacements(Mockito.anyCollection());
    }

    @Test
    public void testProcessLastUpdateBeforeTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = LocalDate.now().minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        Replacement dbReplacement = new Replacement(1, "", "", 0).withLastUpdate(yesterday);
        Map<Integer, Collection<Replacement>> replacementMap = new TreeMap<>();
        replacementMap.put(1, Collections.singleton(dbReplacement));
        Mockito.when(articleService.buildReplacementMapByArticle(Mockito.anyCollection())).thenReturn(replacementMap);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        Mockito.verify(articleService).indexReplacements(Mockito.anyCollection());
    }

    @Test
    public void testProcessNewArticle() {
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        Mockito.when(articleService.buildReplacementMapByArticle(Mockito.anyCollection())).thenReturn(new TreeMap<>());

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        Mockito.verify(articleService).indexReplacements(Mockito.anyCollection());
    }

    @Test
    public void testProcessWithNoReplacementsFound() {
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        Mockito.when(articleService.buildReplacementMapByArticle(Mockito.anyCollection())).thenReturn(new TreeMap<>());
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString())).thenReturn(Collections.emptyList());

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        Mockito.verify(articleService).deleteNotReviewedReplacements(Mockito.anyInt());
    }

}
