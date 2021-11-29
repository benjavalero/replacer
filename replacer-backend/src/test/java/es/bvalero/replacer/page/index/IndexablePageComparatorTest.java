package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IndexablePageComparatorTest {

    private final IndexablePageComparator indexablePageComparator = new IndexablePageComparator();

    @Test
    void testIndexNewPageReplacements() {
        int pageId = new Random().nextInt();
        IndexablePage page = IndexablePage
            .builder()
            .id(IndexablePageId.of(WikipediaLanguage.getDefault(), pageId))
            .replacements(Collections.emptyList())
            .lastUpdate(LocalDate.now())
            .build();

        PageIndexResult result = indexablePageComparator.indexPageReplacements(page, null);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .createPages(Set.of(page))
            .createReplacements(Set.of(IndexableReplacement.ofDummy(page)))
            .build();
        assertEquals(expected, result);
    }

    @Test
    void testIndexNewPage() {
        IndexablePageId pageId = IndexablePageId.of(WikipediaLanguage.getDefault(), 100);
        IndexableReplacement rep1 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("")
            .subtype("")
            .position(0)
            .context("")
            .lastUpdate(LocalDate.now())
            .build(); // New => ADD
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .replacements(List.of(rep1))
            .lastUpdate(LocalDate.now())
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, null);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .createPages(Set.of(page))
            .createReplacements(Set.of(rep1))
            .build();
        assertEquals(expected, toIndex);
    }

    @Test
    void testIndexObsoleteReplacements() {
        IndexablePageId pageId = IndexablePageId.of(WikipediaLanguage.getDefault(), 1);
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .replacements(Collections.emptyList())
            .lastUpdate(LocalDate.now())
            .build();

        // Both obsolete to review or reviewed by system ==> Delete
        // A dummy replacement will be created instead
        IndexableReplacement rep2 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("")
            .subtype("")
            .position(2)
            .context("")
            .lastUpdate(LocalDate.now())
            .build();
        IndexableReplacement rep3 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("")
            .subtype("")
            .position(3)
            .context("")
            .lastUpdate(LocalDate.now())
            .build()
            .setSystemReviewed();
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .replacements(List.of(rep2, rep3))
            .lastUpdate(LocalDate.now())
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .createReplacements(Set.of(IndexableReplacement.ofDummy(page)))
            .deleteReplacements(Set.of(rep2, rep3))
            .build();

        assertEquals(expected, toIndex);
    }

    @Test
    void testIndexPageWithoutReplacements() {
        IndexablePageId pageId = IndexablePageId.of(WikipediaLanguage.getDefault(), 100);
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .replacements(Collections.emptyList())
            .lastUpdate(LocalDate.now())
            .build();
        PageIndexResult result = indexablePageComparator.indexPageReplacements(page, null);

        // Save the dummy replacement
        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .createPages(Set.of(page))
            .createReplacements(Set.of(IndexableReplacement.ofDummy(page)))
            .build();

        assertEquals(expected, result);
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
        IndexablePageId pageId = IndexablePageId.of(WikipediaLanguage.getDefault(), 1);
        IndexableReplacement r1 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("1")
            .subtype("1")
            .position(1)
            .context("")
            .lastUpdate(same)
            .build();
        IndexableReplacement r2 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("2")
            .subtype("2")
            .position(2)
            .context("")
            .lastUpdate(same)
            .build();
        IndexableReplacement r3 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("3")
            .subtype("3")
            .position(3)
            .context("")
            .lastUpdate(same)
            .build();
        IndexableReplacement r4 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("4")
            .subtype("4")
            .position(4)
            .context("")
            .lastUpdate(same)
            .build();
        IndexableReplacement r5 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("5")
            .subtype("5")
            .position(5)
            .context("")
            .lastUpdate(same)
            .build();
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .replacements(List.of(r1, r2, r3, r4, r5))
            .lastUpdate(same)
            .build();

        // Existing replacements in DB
        IndexableReplacement r1db = r1.withTouched(false); // Trick to clone and match with the one found to index
        IndexableReplacement r2db = r2.withReviewer("");
        IndexableReplacement r3db = r3.withLastUpdate(before);
        IndexableReplacement r4db = r4.withReviewer("").withLastUpdate(before);
        IndexableReplacement r6db = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("6")
            .subtype("6")
            .position(6)
            .context("")
            .lastUpdate(same)
            .build();
        IndexableReplacement r7db = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("7")
            .subtype("7")
            .position(7)
            .context("")
            .lastUpdate(same)
            .reviewer("")
            .build();
        IndexableReplacement r8db = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("8")
            .subtype("8")
            .position(8)
            .context("")
            .lastUpdate(same)
            .build()
            .setSystemReviewed();
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .replacements(List.of(r1db, r2db, r3db, r4db, r6db, r7db, r8db))
            .lastUpdate(same)
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .updateReplacements(Set.of(r3db.withLastUpdate(same)))
            .createReplacements(Set.of(r5))
            .deleteReplacements(Set.of(r6db, r8db))
            .build();
        assertEquals(expected, toIndex);
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
        IndexablePageId pageId = IndexablePageId.of(WikipediaLanguage.getDefault(), 1);
        IndexableReplacement r1 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("1")
            .subtype("1")
            .position(1)
            .context("")
            .lastUpdate(same)
            .build();
        IndexableReplacement r2 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("2")
            .subtype("2")
            .position(2)
            .context("")
            .lastUpdate(same)
            .build();
        IndexableReplacement r3 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("3")
            .subtype("3")
            .position(3)
            .context("")
            .lastUpdate(same)
            .build();
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .replacements(List.of(r1, r2, r3))
            .lastUpdate(same)
            .build();

        // Existing replacements in DB
        IndexableReplacement r1db = r1.withLastUpdate(before);
        IndexableReplacement r2db = r2.withReviewer("").withLastUpdate(before);
        IndexableReplacement r4db = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("4")
            .subtype("4")
            .position(4)
            .context("")
            .lastUpdate(before)
            .build();
        IndexableReplacement r5db = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("5")
            .subtype("5")
            .position(5)
            .context("")
            .reviewer("")
            .lastUpdate(before)
            .build();
        IndexableReplacement r6db = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type("6")
            .subtype("6")
            .position(6)
            .context("")
            .lastUpdate(before)
            .build()
            .setSystemReviewed();
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .replacements(List.of(r1db, r2db, r4db, r5db, r6db))
            .lastUpdate(before)
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .updateReplacements(Set.of(r1db.withLastUpdate(same)))
            .createReplacements(Set.of(r3))
            .deleteReplacements(Set.of(r4db, r6db))
            .build();
        assertEquals(expected, toIndex);
    }

    @Test
    void testSeveralDummies() {
        LocalDate same = LocalDate.now();
        LocalDate before = same.minusDays(1);

        IndexablePageId pageId = IndexablePageId.of(WikipediaLanguage.getDefault(), 1);
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .replacements(Collections.emptyList())
            .lastUpdate(same)
            .build();

        // Existing replacements in DB: two dummies, one of them old and obsolete.
        IndexableReplacement r1db = IndexableReplacement.ofDummy(page).withLastUpdate(before);
        IndexableReplacement r2db = IndexableReplacement.ofDummy(page);
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .replacements(List.of(r1db, r2db))
            .lastUpdate(same)
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .deleteReplacements(Set.of(r1db))
            .build();
        assertEquals(expected, toIndex);
    }
}
