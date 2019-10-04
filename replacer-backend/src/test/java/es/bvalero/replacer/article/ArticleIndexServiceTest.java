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
                .save(Mockito.any(ReplacementEntity.class));
    }

    @Test
    public void testIndexNewArticle() {
        ReplacementEntity rep1 = new ReplacementEntity(1, "", "", 1); // New => ADD
        List<ReplacementEntity> newReplacements = Collections.singletonList(rep1);

        List<ReplacementEntity> dbReplacements = Collections.emptyList();

        WikipediaPage article = WikipediaPage.builder().build();
        articleIndexService.indexReplacements(article, newReplacements, dbReplacements);

        Mockito.verify(replacementRepository, Mockito.times(1)).save(rep1);
    }

    @Test
    public void testIndexObsoleteArticle() {
        List<ReplacementEntity> newReplacements = Collections.emptyList();

        ReplacementEntity rep2 = new ReplacementEntity(1, "", "", 2); // Obsolete To review => REVIEW
        ReplacementEntity rep3 = new ReplacementEntity(1, "", "", 3)
                .withReviewer("x"); // Obsolete Reviewed => DO NOTHING
        List<ReplacementEntity> dbReplacements = new ArrayList<>(Arrays.asList(rep2, rep3));

        WikipediaPage article = WikipediaPage.builder().lastUpdate(LocalDateTime.now()).build();
        articleIndexService.indexReplacements(article, newReplacements, dbReplacements);

        Mockito.verify(replacementRepository, Mockito.times(1)).save(
                rep2.withReviewer(ArticleIndexService.SYSTEM_REVIEWER).withLastUpdate(LocalDate.now()));
    }

    @Test
    public void testIndexArticleWithoutReplacements() {
        List<ReplacementEntity> newReplacements = Collections.emptyList();
        List<ReplacementEntity> dbReplacements = Collections.emptyList();

        int articleId = 1;
        WikipediaPage article = WikipediaPage.builder()
                .id(articleId)
                .lastUpdate(LocalDateTime.now())
                .build();
        articleIndexService.indexReplacements(article, newReplacements, dbReplacements);

        // Save the fake replacement
        Mockito.verify(replacementRepository, Mockito.times(1)).save(
                new ReplacementEntity(articleId, "", "", 0)
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
        ReplacementEntity r1 = new ReplacementEntity(1, "1", "1", 1).withLastUpdate(same);
        ReplacementEntity r2 = new ReplacementEntity(1, "2", "2", 2).withLastUpdate(same);
        ReplacementEntity r3 = new ReplacementEntity(1, "3", "3", 3).withLastUpdate(same);
        ReplacementEntity r4 = new ReplacementEntity(1, "4", "4", 4).withLastUpdate(same);
        ReplacementEntity r5 = new ReplacementEntity(1, "5", "5", 5).withLastUpdate(same);
        List<ReplacementEntity> newReplacements = Arrays.asList(r1, r2, r3, r4, r5);

        // Existing replacements in DB
        ReplacementEntity r1db = r1.withReviewer(null);
        ReplacementEntity r2db = r2.withReviewer("");
        ReplacementEntity r3db = r3.withLastUpdate(before);
        ReplacementEntity r4db = r4.withLastUpdate(before).withReviewer("");
        ReplacementEntity r6db = new ReplacementEntity(1, "6", "6", 6).withLastUpdate(same);
        ReplacementEntity r7db = new ReplacementEntity(1, "7", "7", 7)
                .withLastUpdate(same).withReviewer("");
        List<ReplacementEntity> dbReplacements2 = new ArrayList<>(Arrays.asList(r1db, r2db, r3db, r4db, r6db, r7db));

        WikipediaPage article = WikipediaPage.builder().build();
        articleIndexService.indexReplacements(article, newReplacements, dbReplacements2);

        Mockito.verify(replacementRepository, Mockito.times(3)).save(Mockito.any(ReplacementEntity.class));
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
        ReplacementEntity r1 = new ReplacementEntity(1, "1", "1", 1).withLastUpdate(same);
        ReplacementEntity r2 = new ReplacementEntity(1, "2", "2", 2).withLastUpdate(same);
        ReplacementEntity r3 = new ReplacementEntity(1, "3", "3", 3).withLastUpdate(same);
        List<ReplacementEntity> newReplacements = Arrays.asList(r1, r2, r3);

        // Existing replacements in DB
        ReplacementEntity r1db = r1.withLastUpdate(before);
        ReplacementEntity r2db = r2.withLastUpdate(before).withReviewer("");
        ReplacementEntity r4db = new ReplacementEntity(1, "4", "4", 4).withLastUpdate(before);
        ReplacementEntity r5db = new ReplacementEntity(1, "5", "5", 5)
                .withLastUpdate(before).withReviewer("");
        List<ReplacementEntity> dbReplacements2 = new ArrayList<>(Arrays.asList(r1db, r2db, r4db, r5db));

        WikipediaPage article = WikipediaPage.builder().build();
        articleIndexService.indexReplacements(article, newReplacements, dbReplacements2);

        Mockito.verify(replacementRepository, Mockito.times(3)).save(Mockito.any(ReplacementEntity.class));
        Mockito.verify(replacementRepository, Mockito.times(1)).save(r1db.withLastUpdate(same));
        Mockito.verify(replacementRepository, Mockito.times(1)).save(r3);
        Mockito.verify(replacementRepository, Mockito.times(1)).save(
                r4db.withReviewer(ArticleIndexService.SYSTEM_REVIEWER).withLastUpdate(LocalDate.now()));
    }

    @Test
    public void testReviewArticleNoType() {
        int articleId = new Random().nextInt();

        // There is several replacements for the article
        ReplacementEntity rep1 = new ReplacementEntity(articleId, "A", "B", 0);
        ReplacementEntity rep2 = new ReplacementEntity(articleId, "C", "D", 1);
        List<ReplacementEntity> reps = Arrays.asList(rep1, rep2);
        Mockito.when(replacementRepository.findByArticleIdAndReviewerIsNull(articleId))
                .thenReturn(reps);

        articleIndexService.reviewArticle(articleId, null, null, "X");

        Mockito.verify(replacementRepository, Mockito.times(2))
                .save(Mockito.any(ReplacementEntity.class));
    }

    @Test
    public void testReviewArticleWithType() {
        int articleId = new Random().nextInt();

        // There are several replacements for the article
        ReplacementEntity rep1 = new ReplacementEntity(articleId, "A", "B", 0);
        List<ReplacementEntity> reps = Collections.singletonList(rep1);
        Mockito.when(replacementRepository.findByArticleIdAndTypeAndSubtypeAndReviewerIsNull(articleId, "A", "B"))
                .thenReturn(reps);

        articleIndexService.reviewArticle(articleId, "A", "B", "X");

        Mockito.verify(articleStatsService, Mockito.times(1))
                .decreaseCachedReplacementsCount("A", "B", 1);
        Mockito.verify(replacementRepository, Mockito.times(1))
                .save(Mockito.any(ReplacementEntity.class));
    }

    @Test
    public void testReviewArticleWithCustom() {
        int articleId = new Random().nextInt();

        articleIndexService.reviewArticle(articleId, ReplacementFinderService.CUSTOM_FINDER_TYPE, "B", "X");

        Mockito.verify(articleStatsService, Mockito.times(0))
                .decreaseCachedReplacementsCount(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());

        ReplacementEntity toSave = new ReplacementEntity(articleId, ReplacementFinderService.CUSTOM_FINDER_TYPE, "B", 0)
                .withLastUpdate(LocalDate.now()).withReviewer("X");
        Mockito.verify(replacementRepository, Mockito.times(1)).save(toSave);
    }

    @Test
    public void testReviewArticlesAsSystem() {
        int articleId = new Random().nextInt();
        Set<Integer> articleIds = Collections.singleton(articleId);

        ReplacementEntity rep1 = new ReplacementEntity(articleId, "A", "B", 0);
        List<ReplacementEntity> reps = Collections.singletonList(rep1);
        Mockito.when(replacementRepository.findByArticleIdAndReviewerIsNull(articleId))
                .thenReturn(reps);

        articleIndexService.reviewArticlesAsSystem(articleIds);

        ReplacementEntity toSave = new ReplacementEntity(articleId, "A", "B", 0)
                .withLastUpdate(LocalDate.now()).withReviewer(ArticleIndexService.SYSTEM_REVIEWER);
        Mockito.verify(replacementRepository, Mockito.times(1)).save(toSave);
    }

}
