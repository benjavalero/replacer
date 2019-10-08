package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ArticleIndexServiceTest {

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private ArticleStatsService articleStatsService;

    @Spy
    private ModelMapper modelMapper;

    @Spy
    @InjectMocks
    private ArticleIndexService articleIndexService;

    @Before
    public void setUp() {
        articleIndexService = new ArticleIndexService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIndexNewArticleReplacements() {
        Replacement rep1 = Replacement.builder().build();  // New => ADD
        List<Replacement> newReplacements = Collections.singletonList(rep1);

        WikipediaPage article = WikipediaPage.builder().lastUpdate(LocalDateTime.now()).build();
        articleIndexService.indexArticleReplacements(article, newReplacements);

        Mockito.verify(replacementRepository, Mockito.times(1))
                .save(Mockito.any(ReplacementEntity.class));
    }

    @Test
    public void testIndexNewArticle() {
        IndexableReplacement rep1 = new IndexableReplacement(1, "", "", 1, LocalDate.now()); // New => ADD
        List<IndexableReplacement> newReplacements = Collections.singletonList(rep1);

        List<ReplacementEntity> dbReplacements = Collections.emptyList();

        articleIndexService.indexReplacements(newReplacements, dbReplacements);

        Mockito.verify(articleIndexService, Mockito.times(1)).insertReplacement(rep1);
    }

    @Test
    public void testIndexObsoleteArticle() {
        List<IndexableReplacement> newReplacements = Collections.emptyList();

        ReplacementEntity rep2 = new ReplacementEntity(1, "", "", 2); // Obsolete To review => REVIEW
        ReplacementEntity rep3 = new ReplacementEntity(1, "", "", 3);
        rep3.setReviewer("x"); // Obsolete Reviewed => DO NOTHING
        List<ReplacementEntity> dbReplacements = new ArrayList<>(Arrays.asList(rep2, rep3));

        articleIndexService.indexReplacements(newReplacements, dbReplacements);

        Mockito.verify(replacementRepository, Mockito.times(1)).save(rep2);
    }

    @Test
    public void testIndexArticleWithoutReplacements() {
        List<Replacement> newReplacements = Collections.emptyList();
        List<ReplacementEntity> dbReplacements = Collections.emptyList();

        int articleId = 1;
        WikipediaPage article = WikipediaPage.builder()
                .id(articleId)
                .lastUpdate(LocalDateTime.now())
                .build();
        articleIndexService.indexArticleReplacements(article, newReplacements, dbReplacements);

        // Save the fake replacement
        Mockito.verify(replacementRepository, Mockito.times(1)).save(
                new ReplacementEntity(articleId, "", "", 0, ArticleIndexService.SYSTEM_REVIEWER));
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
        IndexableReplacement r1 = new IndexableReplacement(1, "1", "1", 1, same);
        IndexableReplacement r2 = new IndexableReplacement(1, "2", "2", 2, same);
        IndexableReplacement r3 = new IndexableReplacement(1, "3", "3", 3, same);
        IndexableReplacement r4 = new IndexableReplacement(1, "4", "4", 4, same);
        IndexableReplacement r5 = new IndexableReplacement(1, "5", "5", 5, same);
        List<IndexableReplacement> newReplacements = Arrays.asList(r1, r2, r3, r4, r5);

        // Existing replacements in DB
        ReplacementEntity r1db = new ReplacementEntity(1, "1", "1", 1);
        ReplacementEntity r2db = new ReplacementEntity(1, "2", "2", 2, "");
        ReplacementEntity r3db = new ReplacementEntity(1, "3", "3", 3);
        r3db.setLastUpdate(before);
        ReplacementEntity r4db = new ReplacementEntity(1, "4", "4", 4, "");
        ReplacementEntity r6db = new ReplacementEntity(1, "6", "6", 6);
        ReplacementEntity r7db = new ReplacementEntity(1, "7", "7", 7, "");
        List<ReplacementEntity> dbReplacements = new ArrayList<>(Arrays.asList(r1db, r2db, r3db, r4db, r6db, r7db));

        articleIndexService.indexReplacements(newReplacements, dbReplacements);

        Mockito.verify(replacementRepository, Mockito.times(3)).save(Mockito.any(ReplacementEntity.class));
        Mockito.verify(replacementRepository, Mockito.times(1)).save(r3db);
        Mockito.verify(articleIndexService, Mockito.times(1)).insertReplacement(r5);
        Mockito.verify(replacementRepository, Mockito.times(1)).save(r6db);
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
        IndexableReplacement r1 = new IndexableReplacement(1, "1", "1", 1, same);
        IndexableReplacement r2 = new IndexableReplacement(1, "2", "2", 2, same);
        IndexableReplacement r3 = new IndexableReplacement(1, "3", "3", 3, same);
        List<IndexableReplacement> newReplacements = Arrays.asList(r1, r2, r3);

        // Existing replacements in DB
        ReplacementEntity r1db = new ReplacementEntity(1, "1", "1", 1);
        r1db.setLastUpdate(before);
        ReplacementEntity r2db = new ReplacementEntity(1, "2", "2", 2, "");
        r2db.setLastUpdate(before);
        ReplacementEntity r4db = new ReplacementEntity(1, "4", "4", 4);
        r4db.setLastUpdate(before);
        ReplacementEntity r5db = new ReplacementEntity(1, "5", "5", 5, "");
        r5db.setLastUpdate(before);
        List<ReplacementEntity> dbReplacements2 = new ArrayList<>(Arrays.asList(r1db, r2db, r4db, r5db));

        articleIndexService.indexReplacements(newReplacements, dbReplacements2);

        Mockito.verify(replacementRepository, Mockito.times(3)).save(Mockito.any(ReplacementEntity.class));
        Mockito.verify(replacementRepository, Mockito.times(1)).save(r1db);
        Mockito.verify(articleIndexService, Mockito.times(1)).insertReplacement(r3);
        Mockito.verify(replacementRepository, Mockito.times(1)).save(r4db);
    }

    @Test
    public void testReviewArticleNoType() {
        int articleId = new Random().nextInt();

        // There are several replacements for the article
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

        ReplacementEntity toSave = new ReplacementEntity(articleId, ReplacementFinderService.CUSTOM_FINDER_TYPE, "B", 0, "X");
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

        ReplacementEntity toSave = new ReplacementEntity(articleId, "A", "B", 0, ArticleIndexService.SYSTEM_REVIEWER);
        Mockito.verify(replacementRepository, Mockito.times(1)).save(toSave);
    }

}
