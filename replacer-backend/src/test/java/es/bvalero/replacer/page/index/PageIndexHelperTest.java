package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { IndexablePageValidator.class })
class PageIndexHelperTest {

    @Mock
    private ReplacementDao replacementDao;

    @Autowired
    private IndexablePageValidator indexablePageValidator;

    @InjectMocks
    private PageIndexHelper pageIndexHelper;

    @BeforeEach
    void setUp() {
        pageIndexHelper = new PageIndexHelper();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testIndexNewPageReplacements() {
        int pageId = new Random().nextInt();
        IndexablePage page = IndexablePage.builder().id(pageId).lang(WikipediaLanguage.SPANISH).build();

        pageIndexHelper.indexPageReplacements(page, Collections.emptyList());

        verify(replacementDao, times(1)).findByPageId(eq(pageId), any(WikipediaLanguage.class));
    }

    @Test
    void testIndexNewPage() {
        IndexableReplacement rep1 = IndexableReplacement.builder().lang(WikipediaLanguage.SPANISH).title("T").build(); // New => ADD
        IndexablePage page = IndexablePage.builder().lang(WikipediaLanguage.SPANISH).title("T").build();
        List<IndexableReplacement> newReplacements = Collections.singletonList(rep1);

        List<ReplacementEntity> dbReplacements = new ArrayList<>();

        List<ReplacementEntity> toIndex = pageIndexHelper.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        assertEquals(Collections.singletonList(pageIndexHelper.convert(rep1)), toIndex);
    }

    @Test
    void testIndexObsoletePage() {
        List<IndexableReplacement> newReplacements = Collections.emptyList();
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

        List<ReplacementEntity> toIndex = pageIndexHelper.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        assertEquals(
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

        IndexablePage page = IndexablePage.builder().lang(WikipediaLanguage.SPANISH).build();
        List<ReplacementEntity> result = pageIndexHelper.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        // Save the dummy replacement
        assertEquals(1, result.size());
        assertTrue(result.get(0).isDummy());
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

        String title = "T";
        String content = "012345678";
        String context = "012345678"; // The context is the same than content as it is so short
        IndexablePage page = IndexablePage
            .builder()
            .id(1)
            .lang(WikipediaLanguage.getDefault())
            .content(content)
            .lastUpdate(same)
            .title(title)
            .build();

        // Replacements found to index
        IndexableReplacement r1 = IndexableReplacement
            .builder()
            .pageId(1)
            .lang(WikipediaLanguage.getDefault())
            .type("1")
            .subtype("1")
            .position(1)
            .context(context)
            .lastUpdate(same)
            .title(title)
            .build();
        IndexableReplacement r2 = IndexableReplacement
            .builder()
            .pageId(1)
            .lang(WikipediaLanguage.getDefault())
            .type("2")
            .subtype("2")
            .position(2)
            .context(context)
            .lastUpdate(same)
            .title(title)
            .build();
        IndexableReplacement r3 = IndexableReplacement
            .builder()
            .pageId(1)
            .lang(WikipediaLanguage.getDefault())
            .type("3")
            .subtype("3")
            .position(3)
            .context(context)
            .lastUpdate(same)
            .title(title)
            .build();
        IndexableReplacement r4 = IndexableReplacement
            .builder()
            .pageId(1)
            .lang(WikipediaLanguage.getDefault())
            .type("4")
            .subtype("4")
            .position(4)
            .context(context)
            .lastUpdate(same)
            .title(title)
            .build();
        IndexableReplacement r5 = IndexableReplacement
            .builder()
            .pageId(1)
            .lang(WikipediaLanguage.getDefault())
            .type("5")
            .subtype("5")
            .position(5)
            .context(context)
            .lastUpdate(same)
            .title(title)
            .build();
        List<IndexableReplacement> newReplacements = Arrays.asList(r1, r2, r3, r4, r5);

        // Existing replacements in DB
        ReplacementEntity r1db = ReplacementEntity.of(1, "1", "1", 1).withTitle(title).withContext(context); // To match with the one found to index
        ReplacementEntity r2db = ReplacementEntity.of(1, "2", "2", 2).withTitle(title).withReviewer("");
        ReplacementEntity r3db = ReplacementEntity
            .of(1, "3", "3", 3)
            .withTitle(title)
            .withContext(context)
            .withLastUpdate(before);
        ReplacementEntity r4db = ReplacementEntity
            .of(1, "4", "4", 4)
            .withTitle(title)
            .withReviewer(context)
            .withLastUpdate(before);
        ReplacementEntity r6db = ReplacementEntity.of(1, "6", "6", 6).withTitle(title);
        ReplacementEntity r7db = ReplacementEntity.of(1, "7", "7", 7).withTitle(title).withReviewer("");
        ReplacementEntity r8db = ReplacementEntity.of(1, "8", "8", 8).withTitle(title).setSystemReviewed();
        List<ReplacementEntity> dbReplacements = new ArrayList<>(
            Arrays.asList(r1db, r2db, r3db, r4db, r6db, r7db, r8db)
        );

        List<ReplacementEntity> toIndex = pageIndexHelper.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        assertEquals(
            Set.of(r3db.updateLastUpdate(same), pageIndexHelper.convert(r5), r6db.setToDelete(), r8db.setToDelete()),
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

        String title = "T";
        String content = "012345678";
        String context = "012345678"; // The context is the same than content as it is so short
        IndexablePage page = IndexablePage
            .builder()
            .id(1)
            .lang(WikipediaLanguage.getDefault())
            .content(content)
            .lastUpdate(same)
            .title(title)
            .build();

        // Replacements found to index
        IndexableReplacement r1 = IndexableReplacement
            .builder()
            .pageId(1)
            .lang(WikipediaLanguage.getDefault())
            .type("1")
            .subtype("1")
            .position(1)
            .context(context)
            .lastUpdate(same)
            .title(title)
            .build();
        IndexableReplacement r2 = IndexableReplacement
            .builder()
            .pageId(1)
            .lang(WikipediaLanguage.getDefault())
            .type("2")
            .subtype("2")
            .position(2)
            .context(context)
            .lastUpdate(same)
            .title(title)
            .build();
        IndexableReplacement r3 = IndexableReplacement
            .builder()
            .pageId(1)
            .lang(WikipediaLanguage.getDefault())
            .type("3")
            .subtype("3")
            .position(3)
            .context(context)
            .lastUpdate(same)
            .title(title)
            .build();
        List<IndexableReplacement> newReplacements = Arrays.asList(r1, r2, r3);

        // Existing replacements in DB
        ReplacementEntity r1db = ReplacementEntity
            .of(1, "1", "1", 1)
            .withTitle(title)
            .withContext(context)
            .withLastUpdate(before);
        ReplacementEntity r2db = ReplacementEntity
            .of(1, "2", "2", 2)
            .withTitle(title)
            .withReviewer("")
            .withLastUpdate(before);
        ReplacementEntity r4db = ReplacementEntity.of(1, "4", "4", 4).withTitle(title).withLastUpdate(before);
        ReplacementEntity r5db = ReplacementEntity
            .of(1, "5", "5", 5)
            .withTitle(title)
            .withReviewer("")
            .withLastUpdate(before);
        ReplacementEntity r6db = ReplacementEntity
            .of(1, "6", "6", 6)
            .withTitle(title)
            .setSystemReviewed()
            .withLastUpdate(before);
        List<ReplacementEntity> dbReplacements = new ArrayList<>(Arrays.asList(r1db, r2db, r4db, r5db, r6db));

        List<ReplacementEntity> toIndex = pageIndexHelper.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        assertEquals(
            Set.of(r1db.updateLastUpdate(same), pageIndexHelper.convert(r3), r4db.setToDelete(), r6db.setToDelete()),
            new HashSet<>(toIndex)
        );
    }

    @Test
    void testSeveralDummies() {
        LocalDate same = LocalDate.now();
        LocalDate before = same.minusDays(1);

        String title = "T";
        String content = "012345678";
        IndexablePage page = IndexablePage
            .builder()
            .id(1)
            .lang(WikipediaLanguage.getDefault())
            .content(content)
            .lastUpdate(same)
            .title(title)
            .build();

        // No replacements to index
        List<IndexableReplacement> newReplacements = Collections.emptyList();

        // Existing replacements in DB: two dummies, one of them old and obsolete.
        ReplacementEntity r1db = ReplacementEntity.ofDummy(page.getId(), page.getLang(), before);
        ReplacementEntity r2db = ReplacementEntity.ofDummy(page.getId(), page.getLang(), same);
        List<ReplacementEntity> dbReplacements = new ArrayList<>(Arrays.asList(r1db, r2db));

        List<ReplacementEntity> toIndex = pageIndexHelper.findIndexPageReplacements(
            page,
            newReplacements,
            dbReplacements
        );

        assertEquals(Set.of(r1db.setToDelete()), new HashSet<>(toIndex));
    }

    @Test
    void testIndexablePageIsProcessableByNamespace() throws ReplacerException {
        assertThrows(
            ReplacerException.class,
            () ->
                indexablePageValidator.validateProcessableByNamespace(
                    IndexablePage.builder().namespace(WikipediaNamespace.WIKIPEDIA).build()
                )
        );
        indexablePageValidator.validateProcessableByNamespace(
            IndexablePage.builder().namespace(WikipediaNamespace.ARTICLE).build()
        );
        indexablePageValidator.validateProcessableByNamespace(
            IndexablePage.builder().namespace(WikipediaNamespace.ANNEX).build()
        );
    }
}
