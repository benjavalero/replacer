package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ArticleIndexServiceTest {

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private ArticleStatsService articleStatsService;

    @InjectMocks
    private ArticleIndexService articleIndexService;

    @Before
    public void setUp() {
        articleIndexService = new ArticleIndexService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIndexNewArticleReplacements() {
        ArticleReplacement rep1 = ArticleReplacement.builder().build();  // New => ADD
        List<ArticleReplacement> newReplacements = Collections.singletonList(rep1);

        WikipediaPage article = WikipediaPage.builder().lastUpdate(LocalDateTime.now()).build();
        articleIndexService.indexArticleReplacements(article, newReplacements);

        Mockito.verify(replacementRepository, Mockito.times(1))
                .save(Mockito.any(Replacement.class));
    }

    @Test
    public void testIndexNewArticle() {
        Replacement rep1 = new Replacement(1, "", "", 1); // New => ADD
        List<Replacement> newReplacements = Collections.singletonList(rep1);

        List<Replacement> dbReplacements = Collections.emptyList();

        WikipediaPage article = WikipediaPage.builder().build();
        articleIndexService.indexReplacements(article, newReplacements, dbReplacements);

        Mockito.verify(replacementRepository, Mockito.times(1)).save(rep1);
    }

    @Test
    public void testIndexObsoleteArticle() {
        List<Replacement> newReplacements = Collections.emptyList();

        Replacement rep2 = new Replacement(1, "", "", 2); // Obsolete To review => REVIEW
        Replacement rep3 = new Replacement(1, "", "", 3)
                .withReviewer("x"); // Obsolete Reviewed => DO NOTHING
        List<Replacement> dbReplacements = new ArrayList<>(Arrays.asList(rep2, rep3));

        WikipediaPage article = WikipediaPage.builder().lastUpdate(LocalDateTime.now()).build();
        articleIndexService.indexReplacements(article, newReplacements, dbReplacements);

        Mockito.verify(replacementRepository, Mockito.times(1)).save(
                rep2.withReviewer(ArticleIndexService.SYSTEM_REVIEWER).withLastUpdate(LocalDate.now()));
    }

    @Test
    public void testIndexArticleWithoutReplacements() {
        List<Replacement> newReplacements = Collections.emptyList();
        List<Replacement> dbReplacements = Collections.emptyList();

        int articleId = 1;
        WikipediaPage article = WikipediaPage.builder()
                .id(articleId)
                .lastUpdate(LocalDateTime.now())
                .build();
        articleIndexService.indexReplacements(article, newReplacements, dbReplacements);

        // Save the fake replacement
        Mockito.verify(replacementRepository, Mockito.times(1)).save(
                new Replacement(articleId, "", "", 0)
                        .withReviewer(ArticleIndexService.SYSTEM_REVIEWER)
                        .withLastUpdate(LocalDate.now()));
    }

    @Test
    public void testIndexExistingArticleSameDate() {
        LocalDate same = LocalDate.now();
        LocalDate before = same.minusDays(1);

        // R1 : In DB not reviewed => Do nothing
        // R2 : In DB reviewed => Do nothing
        // R3 : In DB older not reviewed => Update timestamp
        // R4 : In DB older reviewed => Do nothing
        // R5 : Not in DB => Add
        // R6 : Only in DB not reviewed => Review
        // R7 : Only in DB reviewed => Do nothing

        // Replacements found to index
        Replacement r1 = new Replacement(1, "1", "1", 1).withLastUpdate(same);
        Replacement r2 = new Replacement(1, "2", "2", 2).withLastUpdate(same);
        Replacement r3 = new Replacement(1, "3", "3", 3).withLastUpdate(same);
        Replacement r4 = new Replacement(1, "4", "4", 4).withLastUpdate(same);
        Replacement r5 = new Replacement(1, "5", "5", 5).withLastUpdate(same);
        List<Replacement> newReplacements = Arrays.asList(r1, r2, r3, r4, r5);

        // Existing replacements in DB
        Replacement r1db = r1.withReviewer(null);
        Replacement r2db = r2.withReviewer("");
        Replacement r3db = r3.withLastUpdate(before);
        Replacement r4db = r4.withLastUpdate(before).withReviewer("");
        Replacement r6db = new Replacement(1, "6", "6", 6).withLastUpdate(same);
        Replacement r7db = new Replacement(1, "7", "7", 7)
                .withLastUpdate(same).withReviewer("");
        List<Replacement> dbReplacements2 = new ArrayList<>(Arrays.asList(r1db, r2db, r3db, r4db, r6db, r7db));

        WikipediaPage article = WikipediaPage.builder().build();
        articleIndexService.indexReplacements(article, newReplacements, dbReplacements2);

        Mockito.verify(replacementRepository, Mockito.times(3)).save(Mockito.any(Replacement.class));
        Mockito.verify(replacementRepository, Mockito.times(1)).save(r3db.withLastUpdate(same));
        Mockito.verify(replacementRepository, Mockito.times(1)).save(r5);
        Mockito.verify(replacementRepository, Mockito.times(1)).save(
                r6db.withReviewer(ArticleIndexService.SYSTEM_REVIEWER).withLastUpdate(LocalDate.now()));
    }

    @Test
    public void testIndexExistingArticleDateAfter() {
        LocalDate same = LocalDate.now();
        LocalDate before = same.minusDays(1);

        // R1 : In DB older not reviewed => Update timestamp
        // R2 : In DB older reviewed => Do nothing
        // R3 : Not in DB => Add
        // R6 : Only in DB not reviewed => Review
        // R7 : Only in DB reviewed => Do nothing

        // Replacements found to index
        Replacement r1 = new Replacement(1, "1", "1", 1).withLastUpdate(same);
        Replacement r2 = new Replacement(1, "2", "2", 2).withLastUpdate(same);
        Replacement r3 = new Replacement(1, "3", "3", 3).withLastUpdate(same);
        List<Replacement> newReplacements = Arrays.asList(r1, r2, r3);

        // Existing replacements in DB
        Replacement r1db = r1.withLastUpdate(before);
        Replacement r2db = r2.withLastUpdate(before).withReviewer("");
        Replacement r4db = new Replacement(1, "4", "4", 4).withLastUpdate(before);
        Replacement r5db = new Replacement(1, "5", "5", 5)
                .withLastUpdate(before).withReviewer("");
        List<Replacement> dbReplacements2 = new ArrayList<>(Arrays.asList(r1db, r2db, r4db, r5db));

        WikipediaPage article = WikipediaPage.builder().build();
        articleIndexService.indexReplacements(article, newReplacements, dbReplacements2);

        Mockito.verify(replacementRepository, Mockito.times(3)).save(Mockito.any(Replacement.class));
        Mockito.verify(replacementRepository, Mockito.times(1)).save(r1db.withLastUpdate(same));
        Mockito.verify(replacementRepository, Mockito.times(1)).save(r3);
        Mockito.verify(replacementRepository, Mockito.times(1)).save(
                r4db.withReviewer(ArticleIndexService.SYSTEM_REVIEWER).withLastUpdate(LocalDate.now()));
    }

    @Test
    public void testReviewArticleNoType() {
        int articleId = new Random().nextInt();

        // There is several replacements for the article
        Replacement rep1 = new Replacement(articleId, "A", "B", 0);
        Replacement rep2 = new Replacement(articleId, "C", "D", 1);
        List<Replacement> reps = Arrays.asList(rep1, rep2);
        Mockito.when(replacementRepository.findByArticleIdAndReviewerIsNull(articleId))
                .thenReturn(reps);

        articleIndexService.reviewArticle(articleId, null, null, "X");

        Mockito.verify(replacementRepository, Mockito.times(2))
                .save(Mockito.any(Replacement.class));
    }

    @Test
    public void testReviewArticleWithType() {
        int articleId = new Random().nextInt();

        // There are several replacements for the article
        Replacement rep1 = new Replacement(articleId, "A", "B", 0);
        List<Replacement> reps = Collections.singletonList(rep1);
        Mockito.when(replacementRepository.findByArticleIdAndTypeAndSubtypeAndReviewerIsNull(articleId, "A", "B"))
                .thenReturn(reps);

        articleIndexService.reviewArticle(articleId, "A", "B", "X");

        Mockito.verify(articleStatsService, Mockito.times(1))
                .decreaseCachedReplacementsCount("A", "B", 1);
        Mockito.verify(replacementRepository, Mockito.times(1))
                .save(Mockito.any(Replacement.class));
    }

    @Test
    public void testReviewArticleWithCustom() {
        int articleId = new Random().nextInt();

        articleIndexService.reviewArticle(articleId, ReplacementFinderService.CUSTOM_FINDER_TYPE, "B", "X");

        Mockito.verify(articleStatsService, Mockito.times(0))
                .decreaseCachedReplacementsCount(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());

        Replacement toSave = new Replacement(articleId, ReplacementFinderService.CUSTOM_FINDER_TYPE, "B", 0)
                .withLastUpdate(LocalDate.now()).withReviewer("X");
        Mockito.verify(replacementRepository, Mockito.times(1)).save(toSave);
    }

    @Test
    public void testReviewArticlesAsSystem() {
        int articleId = new Random().nextInt();
        Set<Integer> articleIds = Collections.singleton(articleId);

        Replacement rep1 = new Replacement(articleId, "A", "B", 0);
        List<Replacement> reps = Collections.singletonList(rep1);
        Mockito.when(replacementRepository.findByArticleIdAndReviewerIsNull(articleId))
                .thenReturn(reps);

        articleIndexService.reviewArticlesAsSystem(articleIds);

        Replacement toSave = new Replacement(articleId, "A", "B", 0)
                .withLastUpdate(LocalDate.now()).withReviewer(ArticleIndexService.SYSTEM_REVIEWER);
        Mockito.verify(replacementRepository, Mockito.times(1)).save(toSave);
    }

}
