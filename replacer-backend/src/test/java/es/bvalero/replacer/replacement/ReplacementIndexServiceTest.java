package es.bvalero.replacer.replacement;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class ReplacementIndexServiceTest {

    @Mock
    private ReplacementDao replacementDao;

    @InjectMocks
    private ReplacementIndexService replacementIndexService;

    @BeforeEach
    void setUp() {
        replacementIndexService = new ReplacementIndexService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testIndexNewPageReplacements() {
        int pageId = new Random().nextInt();
        WikipediaPage page = WikipediaPage.builder().id(pageId).lang(WikipediaLanguage.SPANISH).build();

        replacementIndexService.indexPageReplacements(page, Collections.emptyList());

        Mockito
            .verify(replacementDao, Mockito.times(1))
            .findByPageId(Mockito.eq(pageId), Mockito.any(WikipediaLanguage.class));
    }

    @Test
    void testIndexNewPageInvalidReplacements() {
        int pageId = new Random().nextInt();
        int wrongId = pageId + 1;
        WikipediaPage page = WikipediaPage.builder().id(pageId).lang(WikipediaLanguage.SPANISH).build();

        IndexableReplacement indexableReplacement = IndexableReplacement.of(
            wrongId,
            WikipediaLanguage.SPANISH,
            "",
            "",
            0,
            "",
            LocalDate.now(),
            ""
        );

        List<IndexableReplacement> replacements = Collections.singletonList(indexableReplacement);
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> replacementIndexService.indexPageReplacements(page, replacements)
        );
    }

    @Test
    void testIndexNewPage() {
        Replacement rep1 = Replacement.builder().build(); // New => ADD
        WikipediaPage page = WikipediaPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .lastUpdate(LocalDate.now())
            .build();
        IndexableReplacement idx1 = page.convertReplacementToIndexed(rep1);
        List<IndexableReplacement> newReplacements = Collections.singletonList(idx1);

        List<ReplacementEntity> dbReplacements = new ArrayList<>();

        List<ReplacementEntity> toIndex = replacementIndexService.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        Assertions.assertEquals(Collections.singletonList(replacementIndexService.convertToEntity(idx1)), toIndex);
    }

    @Test
    void testIndexObsoletePage() {
        List<IndexableReplacement> newReplacements = Collections.emptyList();
        int pageId = new Random().nextInt();
        WikipediaPage page = WikipediaPage
            .builder()
            .id(pageId)
            .lang(WikipediaLanguage.SPANISH)
            .lastUpdate(LocalDate.now())
            .build();

        // Both obsolete to review or reviewed by system ==> Delete
        // A dummy replacement will be created instead
        ReplacementEntity rep2 = ReplacementEntity.of(1, "", "", 2);
        ReplacementEntity rep3 = ReplacementEntity.of(1, "", "", 3).withReviewer(ReplacementEntity.REVIEWER_SYSTEM);
        List<ReplacementEntity> dbReplacements = new ArrayList<>(Arrays.asList(rep2, rep3));

        List<ReplacementEntity> toIndex = replacementIndexService.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        Assertions.assertEquals(
            Set.of(
                ReplacementEntity.ofDummy(pageId, WikipediaLanguage.SPANISH, LocalDate.now()),
                rep2.setToDelete(),
                rep3.setToDelete()
            ),
            new HashSet<>(toIndex)
        );
    }

    @Test
    void testIndexPageWithoutReplacements() {
        List<IndexableReplacement> newReplacements = Collections.emptyList();
        List<ReplacementEntity> dbReplacements = Collections.emptyList();

        WikipediaPage page = WikipediaPage.builder().lang(WikipediaLanguage.SPANISH).build();
        List<ReplacementEntity> result = replacementIndexService.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        // Save the dummy replacement
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.get(0).isDummy());
    }

    @Test
    void testIndexExistingPageSameDate() {
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
        ReplacementEntity r1db = ReplacementEntity.of(1, "1", "1", 1).withContext(""); // To match with the one found to index
        ReplacementEntity r2db = ReplacementEntity.of(1, "2", "2", 2).withReviewer("");
        ReplacementEntity r3db = ReplacementEntity.of(1, "3", "3", 3).withContext("").withLastUpdate(before);
        ReplacementEntity r4db = ReplacementEntity.of(1, "4", "4", 4).withReviewer("").withLastUpdate(before);
        ReplacementEntity r6db = ReplacementEntity.of(1, "6", "6", 6);
        ReplacementEntity r7db = ReplacementEntity.of(1, "7", "7", 7).withReviewer("");
        ReplacementEntity r8db = ReplacementEntity.of(1, "8", "8", 2).withReviewer(ReplacementEntity.REVIEWER_SYSTEM);
        List<ReplacementEntity> dbReplacements = new ArrayList<>(
            Arrays.asList(r1db, r2db, r3db, r4db, r6db, r7db, r8db)
        );

        WikipediaPage page = WikipediaPage.builder().id(1).lang(WikipediaLanguage.SPANISH).build();
        List<ReplacementEntity> toIndex = replacementIndexService.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        Assertions.assertEquals(
            Set.of(
                r3db.updateLastUpdate(same),
                replacementIndexService.convertToEntity(r5),
                r6db.setToDelete(),
                r8db.setToDelete()
            ),
            new HashSet<>(toIndex)
        );
    }

    @Test
    void testIndexExistingPageDateAfter() {
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
        ReplacementEntity r1db = ReplacementEntity.of(1, "1", "1", 1).withContext("").withLastUpdate(before);
        ReplacementEntity r2db = ReplacementEntity.of(1, "2", "2", 2).withReviewer("").withLastUpdate(before);
        ReplacementEntity r4db = ReplacementEntity.of(1, "4", "4", 4).withLastUpdate(before);
        ReplacementEntity r5db = ReplacementEntity.of(1, "5", "5", 5).withReviewer("").withLastUpdate(before);
        ReplacementEntity r6db = ReplacementEntity
            .of(1, "6", "6", 6)
            .withReviewer(ReplacementEntity.REVIEWER_SYSTEM)
            .withLastUpdate(before);
        List<ReplacementEntity> dbReplacements = new ArrayList<>(Arrays.asList(r1db, r2db, r4db, r5db, r6db));

        WikipediaPage page = WikipediaPage.builder().id(1).lang(WikipediaLanguage.SPANISH).build();
        List<ReplacementEntity> toIndex = replacementIndexService.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        Assertions.assertEquals(
            Set.of(
                r1db.updateLastUpdate(same),
                replacementIndexService.convertToEntity(r3),
                r4db.setToDelete(),
                r6db.setToDelete()
            ),
            new HashSet<>(toIndex)
        );
    }
}
