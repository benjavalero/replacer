package es.bvalero.replacer.replacement;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;

public class ReplacementIndexServiceTest {

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private ReplacementCountService replacementCountService;

    @Spy
    private ModelMapper modelMapper;

    @Spy // To verify the calls to methods
    @InjectMocks
    private ReplacementIndexService replacementIndexService;

    @Before
    public void setUp() {
        replacementIndexService = new ReplacementIndexService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIndexNewArticleReplacements() {
        int articleId = new Random().nextInt();

        replacementIndexService.indexArticleReplacements(articleId, WikipediaLanguage.SPANISH, Collections.emptyList());

        Mockito.verify(replacementRepository, Mockito.times(1)).findByArticleIdAndLang(Mockito.eq(articleId), Mockito.anyString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndexNewArticleInvalidReplacements() {
        int articleId = new Random().nextInt();
        int wrongId = articleId + 1;

        IndexableReplacement indexableReplacement = IndexableReplacement.of(wrongId, WikipediaLanguage.SPANISH, "", "", 0, "", LocalDate.now());
        replacementIndexService.indexArticleReplacements(articleId, WikipediaLanguage.SPANISH, Collections.singletonList(indexableReplacement));
    }

    @Test
    public void testIndexNewArticle() {
        Replacement rep1 = Replacement.builder().build();  // New => ADD
        WikipediaPage article = WikipediaPage.builder().lang(WikipediaLanguage.SPANISH).lastUpdate(LocalDate.now()).build();
        IndexableReplacement idx1 = article.convertReplacementToIndexed(rep1);
        List<IndexableReplacement> newReplacements = Collections.singletonList(idx1);

        List<ReplacementEntity> dbReplacements = Collections.emptyList();

        List<ReplacementEntity> toIndex =
            replacementIndexService.findIndexArticleReplacements(article.getId(), WikipediaLanguage.SPANISH, newReplacements, dbReplacements);

        Assert.assertThat(toIndex, is(Collections.singletonList(replacementIndexService.convertToEntity(idx1))));
    }

    @Test
    public void testIndexObsoleteArticle() {
        List<IndexableReplacement> newReplacements = Collections.emptyList();

        ReplacementEntity rep2 = new ReplacementEntity(1, "", "", 2); // Obsolete To review => REVIEW
        ReplacementEntity rep3 = new ReplacementEntity(1, "", "", 3);
        rep3.setReviewer("x"); // Obsolete Reviewed => DO NOTHING
        List<ReplacementEntity> dbReplacements = new ArrayList<>(Arrays.asList(rep2, rep3));

        List<ReplacementEntity> toIndex =
            replacementIndexService.findIndexArticleReplacements(1, WikipediaLanguage.SPANISH, newReplacements, dbReplacements);

        Assert.assertThat(toIndex, is(Collections.singletonList(rep2)));
    }

    @Test
    public void testIndexArticleWithoutReplacements() {
        List<IndexableReplacement> newReplacements = Collections.emptyList();
        List<ReplacementEntity> dbReplacements = Collections.emptyList();

        int articleId = 1;
        replacementIndexService.findIndexArticleReplacements(articleId, WikipediaLanguage.SPANISH, newReplacements, dbReplacements);

        // Save the fake replacement
        Mockito.verify(replacementIndexService, Mockito.times(1))
            .createFakeReviewedReplacement(Mockito.eq(articleId), Mockito.any(WikipediaLanguage.class));
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
        IndexableReplacement r1 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "1", "1", 1, "", same);
        IndexableReplacement r2 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "2", "2", 2, "", same);
        IndexableReplacement r3 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "3", "3", 3, "", same);
        IndexableReplacement r4 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "4", "4", 4, "", same);
        IndexableReplacement r5 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "5", "5", 5, "", same);
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

        List<ReplacementEntity> toIndex =
            replacementIndexService.findIndexArticleReplacements(1, WikipediaLanguage.SPANISH, newReplacements, dbReplacements);

        Assert.assertThat(toIndex, is(Arrays.asList(r3db, replacementIndexService.convertToEntity(r5), r6db)));
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
        IndexableReplacement r1 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "1", "1", 1, "", same);
        IndexableReplacement r2 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "2", "2", 2, "", same);
        IndexableReplacement r3 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "3", "3", 3, "", same);
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

        List<ReplacementEntity> toIndex =
            replacementIndexService.findIndexArticleReplacements(1, WikipediaLanguage.SPANISH, newReplacements, dbReplacements2);

        Assert.assertThat(toIndex, is(Arrays.asList(r1db, replacementIndexService.convertToEntity(r3), r4db)));
    }

    @Test
    public void testReviewArticleNoType() {
        int articleId = new Random().nextInt();

        // There are several replacements for the article
        ReplacementEntity rep1 = new ReplacementEntity(articleId, "A", "B", 0);
        ReplacementEntity rep2 = new ReplacementEntity(articleId, "C", "D", 1);
        List<ReplacementEntity> reps = Arrays.asList(rep1, rep2);
        Mockito.when(replacementRepository.findByArticleIdAndLangAndReviewerIsNull(articleId, WikipediaLanguage.SPANISH.getCode()))
                .thenReturn(reps);

        replacementIndexService.reviewArticleReplacements(articleId, WikipediaLanguage.SPANISH, null, null, "X");

        Mockito.verify(replacementRepository, Mockito.times(1))
                .saveAll(Mockito.anyIterable());
    }

    @Test
    public void testReviewArticleWithType() {
        int articleId = new Random().nextInt();

        // There are several replacements for the article
        ReplacementEntity rep1 = new ReplacementEntity(articleId, "A", "B", 0);
        List<ReplacementEntity> reps = Collections.singletonList(rep1);
        Mockito.when(replacementRepository.findByArticleIdAndLangAndTypeAndSubtypeAndReviewerIsNull(articleId, WikipediaLanguage.SPANISH.getCode(), "A", "B"))
                .thenReturn(reps);

        replacementIndexService.reviewArticleReplacements(articleId, WikipediaLanguage.SPANISH, "A", "B", "X");

        Mockito.verify(replacementCountService, Mockito.times(1))
                .decreaseCachedReplacementsCount(WikipediaLanguage.SPANISH, "A", "B", 1);
        Mockito.verify(replacementRepository, Mockito.times(1))
                .saveAll(Mockito.anyIterable());
    }

    @Test
    public void testReviewArticleWithCustom() {
        int articleId = new Random().nextInt();

        replacementIndexService.reviewArticleReplacements(articleId, WikipediaLanguage.SPANISH, ReplacementFindService.CUSTOM_FINDER_TYPE, "B", "X");

        Mockito.verify(replacementCountService, Mockito.times(0))
                .decreaseCachedReplacementsCount(Mockito.any(WikipediaLanguage.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());

        ReplacementEntity toSave = new ReplacementEntity(articleId, ReplacementFindService.CUSTOM_FINDER_TYPE, "B", 0, "X");
        toSave.setLang(WikipediaLanguage.SPANISH.getCode());
        Mockito.verify(replacementRepository, Mockito.times(1)).saveAll(Collections.singletonList(toSave));
    }

}
