package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import es.bvalero.replacer.finder.replacement.Replacement;
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
        IndexablePage page = IndexablePage.builder().id(pageId).lang(WikipediaLanguage.SPANISH).build();

        replacementIndexService.indexPageReplacements(page, Collections.emptyList());

        Mockito
            .verify(replacementDao, Mockito.times(1))
            .findByPageId(Mockito.eq(pageId), Mockito.any(WikipediaLanguage.class));
    }

    @Test
    void testIndexNewPage() {
        Replacement rep1 = Replacement.builder().start(0).text("X").build(); // New => ADD
        IndexablePage page = IndexablePage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .content("X")
            .lastUpdate(LocalDate.now())
            .build();
        List<Replacement> newReplacements = Collections.singletonList(rep1);

        List<ReplacementEntity> dbReplacements = new ArrayList<>();

        List<ReplacementEntity> toIndex = replacementIndexService.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        IndexableReplacement idx1 = replacementIndexService.convertToIndexable(page, rep1);
        Assertions.assertEquals(Collections.singletonList(replacementIndexService.convertToEntity(idx1)), toIndex);
    }

    @Test
    void testIndexObsoletePage() {
        List<Replacement> newReplacements = Collections.emptyList();
        int pageId = new Random().nextInt();
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .lang(WikipediaLanguage.SPANISH)
            .lastUpdate(LocalDate.now())
            .build();

        // Both obsolete to review or reviewed by system ==> Delete
        // A dummy replacement will be created instead
        ReplacementEntity rep2 = ReplacementEntity.of(1, "", "", 2);
        ReplacementEntity rep3 = ReplacementEntity.of(1, "", "", 3).setSystemReviewed();
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
        List<Replacement> newReplacements = Collections.emptyList();
        List<ReplacementEntity> dbReplacements = Collections.emptyList();

        IndexablePage page = IndexablePage.builder().lang(WikipediaLanguage.SPANISH).build();
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

        String content = "012345678";
        String context = "012345678"; // The context is the same than content as it is so short
        IndexablePage page = IndexablePage
            .builder()
            .id(1)
            .lang(WikipediaLanguage.getDefault())
            .content(content)
            .lastUpdate(same)
            .build();

        // Replacements found to index
        Replacement r1 = Replacement.builder().type("1").subtype("1").start(1).text("1").build();
        Replacement r2 = Replacement.builder().type("2").subtype("2").start(2).text("2").build();
        Replacement r3 = Replacement.builder().type("3").subtype("3").start(3).text("3").build();
        Replacement r4 = Replacement.builder().type("4").subtype("4").start(4).text("4").build();
        Replacement r5 = Replacement.builder().type("5").subtype("5").start(5).text("5").build();
        List<Replacement> newReplacements = Arrays.asList(r1, r2, r3, r4, r5);

        // Existing replacements in DB
        ReplacementEntity r1db = ReplacementEntity.of(1, "1", "1", 1).withContext(context); // To match with the one found to index
        ReplacementEntity r2db = ReplacementEntity.of(1, "2", "2", 2).withReviewer("");
        ReplacementEntity r3db = ReplacementEntity.of(1, "3", "3", 3).withContext(context).withLastUpdate(before);
        ReplacementEntity r4db = ReplacementEntity.of(1, "4", "4", 4).withReviewer(context).withLastUpdate(before);
        ReplacementEntity r6db = ReplacementEntity.of(1, "6", "6", 6);
        ReplacementEntity r7db = ReplacementEntity.of(1, "7", "7", 7).withReviewer("");
        ReplacementEntity r8db = ReplacementEntity.of(1, "8", "8", 2).setSystemReviewed();
        List<ReplacementEntity> dbReplacements = new ArrayList<>(
            Arrays.asList(r1db, r2db, r3db, r4db, r6db, r7db, r8db)
        );

        List<ReplacementEntity> toIndex = replacementIndexService.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        Assertions.assertEquals(
            Set.of(
                r3db.updateLastUpdate(same),
                replacementIndexService.convertToEntity(replacementIndexService.convertToIndexable(page, r5)),
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

        String content = "012345678";
        String context = "012345678"; // The context is the same than content as it is so short
        IndexablePage page = IndexablePage
            .builder()
            .id(1)
            .lang(WikipediaLanguage.getDefault())
            .content(content)
            .lastUpdate(same)
            .build();

        // Replacements found to index
        Replacement r1 = Replacement.builder().type("1").subtype("1").start(1).text("1").build();
        Replacement r2 = Replacement.builder().type("2").subtype("2").start(2).text("2").build();
        Replacement r3 = Replacement.builder().type("3").subtype("3").start(3).text("3").build();
        List<Replacement> newReplacements = Arrays.asList(r1, r2, r3);

        // Existing replacements in DB
        ReplacementEntity r1db = ReplacementEntity.of(1, "1", "1", 1).withContext(context).withLastUpdate(before);
        ReplacementEntity r2db = ReplacementEntity.of(1, "2", "2", 2).withReviewer("").withLastUpdate(before);
        ReplacementEntity r4db = ReplacementEntity.of(1, "4", "4", 4).withLastUpdate(before);
        ReplacementEntity r5db = ReplacementEntity.of(1, "5", "5", 5).withReviewer("").withLastUpdate(before);
        ReplacementEntity r6db = ReplacementEntity.of(1, "6", "6", 6).setSystemReviewed().withLastUpdate(before);
        List<ReplacementEntity> dbReplacements = new ArrayList<>(Arrays.asList(r1db, r2db, r4db, r5db, r6db));

        List<ReplacementEntity> toIndex = replacementIndexService.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        Assertions.assertEquals(
            Set.of(
                r1db.updateLastUpdate(same),
                replacementIndexService.convertToEntity(replacementIndexService.convertToIndexable(page, r3)),
                r4db.setToDelete(),
                r6db.setToDelete()
            ),
            new HashSet<>(toIndex)
        );
    }

    @Test
    void testIndexablePageIsProcessableByNamespace() throws ReplacerException {
        Assertions.assertThrows(
            ReplacerException.class,
            () ->
                IndexablePage.builder().namespace(WikipediaNamespace.WIKIPEDIA).build().validateProcessableByNamespace()
        );
        IndexablePage.builder().namespace(WikipediaNamespace.ARTICLE).build().validateProcessableByNamespace();
        IndexablePage.builder().namespace(WikipediaNamespace.ANNEX).build().validateProcessableByNamespace();
    }
}
