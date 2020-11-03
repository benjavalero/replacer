package es.bvalero.replacer.replacement;

import es.bvalero.replacer.finder.Replacement;
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
    private ReplacementDao replacementDao;

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
    public void testIndexNewPageReplacements() {
        int pageId = new Random().nextInt();

        replacementIndexService.indexPageReplacements(pageId, WikipediaLanguage.SPANISH, Collections.emptyList());

        Mockito.verify(replacementDao, Mockito.times(1)).findByPageId(Mockito.eq(pageId), Mockito.any(WikipediaLanguage.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndexNewPageInvalidReplacements() {
        int pageId = new Random().nextInt();
        int wrongId = pageId + 1;

        IndexableReplacement indexableReplacement = IndexableReplacement.of(wrongId, WikipediaLanguage.SPANISH, "", "", 0, "", LocalDate.now(), "");
        replacementIndexService.indexPageReplacements(pageId, WikipediaLanguage.SPANISH, Collections.singletonList(indexableReplacement));
    }

    @Test
    public void testIndexNewPage() {
        Replacement rep1 = Replacement.builder().build();  // New => ADD
        WikipediaPage page = WikipediaPage.builder().lang(WikipediaLanguage.SPANISH).lastUpdate(LocalDate.now()).build();
        IndexableReplacement idx1 = page.convertReplacementToIndexed(rep1);
        List<IndexableReplacement> newReplacements = Collections.singletonList(idx1);

        List<ReplacementEntity> dbReplacements = Collections.emptyList();

        List<ReplacementEntity> toIndex =
            replacementIndexService.findIndexPageReplacements(page.getId(), WikipediaLanguage.SPANISH, newReplacements, dbReplacements);

        Assert.assertThat(toIndex, is(Collections.singletonList(replacementIndexService.convertToEntity(idx1))));
    }

    @Test
    public void testIndexObsoletePage() {
        List<IndexableReplacement> newReplacements = Collections.emptyList();

        // Both obsolete to review or reviewed by system ==> Delete
        // A fake replacement will be created instead
        ReplacementEntity rep2 = new ReplacementEntity(1, "", "", 2);
        ReplacementEntity rep3 = new ReplacementEntity(1, "", "", 3, "system");
        List<ReplacementEntity> dbReplacements = new ArrayList<>(Arrays.asList(rep2, rep3));

        List<ReplacementEntity> toIndex =
            replacementIndexService.findIndexPageReplacements(1, WikipediaLanguage.SPANISH, newReplacements, dbReplacements);

        Assert.assertThat(toIndex, is(List.of(
            replacementIndexService.createFakeReviewedReplacement(1, WikipediaLanguage.SPANISH),
            replacementIndexService.setToDelete(rep2),
            replacementIndexService.setToDelete(rep3)
        )));
    }

    @Test
    public void testIndexPageWithoutReplacements() {
        List<IndexableReplacement> newReplacements = Collections.emptyList();
        List<ReplacementEntity> dbReplacements = Collections.emptyList();

        int pageId = 1;
        replacementIndexService.findIndexPageReplacements(pageId, WikipediaLanguage.SPANISH, newReplacements, dbReplacements);

        // Save the fake replacement
        Mockito.verify(replacementIndexService, Mockito.times(1))
            .createFakeReviewedReplacement(Mockito.eq(pageId), Mockito.any(WikipediaLanguage.class));
    }

    @Test
    public void testIndexExistingPageSameDate() {
        LocalDate same = LocalDate.now();
        LocalDate before = same.minusDays(1);

        // R1 : In DB not reviewed => Do nothing
        // R2 : In DB reviewed => Do nothing
        // R3 : In DB older not reviewed => Update timestamp
        // R4 : In DB older reviewed => Do nothing
        // R5 : Not in DB => Add
        // R6 : Only in DB not reviewed => Delete
        // R7 : Only in DB reviewed by user => Do nothing
        // R8 : Only in DB reviewed by system => Delete

        // Replacements found to index
        IndexableReplacement r1 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "1", "1", 1, "", same, "1");
        IndexableReplacement r2 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "2", "2", 2, "", same, "1");
        IndexableReplacement r3 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "3", "3", 3, "", same, "1");
        IndexableReplacement r4 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "4", "4", 4, "", same, "1");
        IndexableReplacement r5 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "5", "5", 5, "", same, "1");
        List<IndexableReplacement> newReplacements = Arrays.asList(r1, r2, r3, r4, r5);

        // Existing replacements in DB
        ReplacementEntity r1db = new ReplacementEntity(1, "1", "1", 1);
        r1db.setContext(""); // To match with the one found to index
        ReplacementEntity r2db = new ReplacementEntity(1, "2", "2", 2, "");
        ReplacementEntity r3db = new ReplacementEntity(1, "3", "3", 3);
        r3db.setLastUpdate(before);
        ReplacementEntity r4db = new ReplacementEntity(1, "4", "4", 4, "");
        r4db.setLastUpdate(before);
        ReplacementEntity r6db = new ReplacementEntity(1, "6", "6", 6);
        ReplacementEntity r7db = new ReplacementEntity(1, "7", "7", 7, "");
        ReplacementEntity r8db = new ReplacementEntity(1, "8", "8", 2, "system");
        List<ReplacementEntity> dbReplacements = new ArrayList<>(Arrays.asList(r1db, r2db, r3db, r4db, r6db, r7db, r8db));

        List<ReplacementEntity> toIndex =
            replacementIndexService.findIndexPageReplacements(1, WikipediaLanguage.SPANISH, newReplacements, dbReplacements);

        Assert.assertThat(toIndex, is(Arrays.asList(
            r3db,
            replacementIndexService.convertToEntity(r5),
            replacementIndexService.setToDelete(r6db),
            replacementIndexService.setToDelete(r8db))));
    }

    @Test
    public void testIndexExistingPageDateAfter() {
        LocalDate same = LocalDate.now();
        LocalDate before = same.minusDays(1);

        // R1 : In DB older not reviewed => Update timestamp
        // R2 : In DB older reviewed => Do nothing
        // R3 : Not in DB => Add
        // R4 : Only in DB not reviewed => Delete
        // R5 : Only in DB reviewed by user => Do nothing
        // R6 : Only in DB reviewed by system => Delete

        // Replacements found to index
        IndexableReplacement r1 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "1", "1", 1, "", same, "1");
        IndexableReplacement r2 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "2", "2", 2, "", same, "1");
        IndexableReplacement r3 = IndexableReplacement.of(1, WikipediaLanguage.SPANISH, "3", "3", 3, "", same, "1");
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
        ReplacementEntity r6db = new ReplacementEntity(1, "6", "6", 6, "system");
        r6db.setLastUpdate(before);
        List<ReplacementEntity> dbReplacements = new ArrayList<>(Arrays.asList(r1db, r2db, r4db, r5db, r6db));

        List<ReplacementEntity> toIndex =
            replacementIndexService.findIndexPageReplacements(1, WikipediaLanguage.SPANISH, newReplacements, dbReplacements);

        Assert.assertThat(toIndex, is(Arrays.asList(
            r1db,
            replacementIndexService.convertToEntity(r3),
            replacementIndexService.setToDelete(r4db),
            replacementIndexService.setToDelete(r6db))));
    }

    @Test
    public void testReviewPageNoType() {
        int pageId = new Random().nextInt();

        replacementIndexService.reviewPageReplacements(pageId, WikipediaLanguage.SPANISH, null, null, "X");

        Mockito.verify(replacementDao, Mockito.times(1))
            .reviewByPageId(WikipediaLanguage.SPANISH, pageId, null, null, "X");
    }

    @Test
    public void testReviewPageWithType() {
        int pageId = new Random().nextInt();

        replacementIndexService.reviewPageReplacements(pageId, WikipediaLanguage.SPANISH, "A", "B", "X");

        Mockito.verify(replacementCountService, Mockito.times(1))
                .decreaseCachedReplacementsCount(WikipediaLanguage.SPANISH, "A", "B", 1);
        Mockito.verify(replacementDao, Mockito.times(1))
                .reviewByPageId(WikipediaLanguage.SPANISH, pageId, "A", "B", "X");
    }

    @Test
    public void testReviewPageWithCustom() {
        int pageId = new Random().nextInt();

        replacementIndexService.reviewPageReplacements(pageId, WikipediaLanguage.SPANISH, ReplacementEntity.TYPE_CUSTOM, "B", "X");

        Mockito.verify(replacementCountService, Mockito.times(0))
                .decreaseCachedReplacementsCount(Mockito.any(WikipediaLanguage.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
        Mockito.verify(replacementDao, Mockito.times(0))
            .reviewByPageId(WikipediaLanguage.SPANISH, pageId, ReplacementEntity.TYPE_CUSTOM, "B", "X");

        ReplacementEntity toSave = replacementIndexService.createCustomReviewedReplacement(pageId, WikipediaLanguage.SPANISH, "B", "X");
        Mockito.verify(replacementDao, Mockito.times(1)).insert(toSave);
    }

}
