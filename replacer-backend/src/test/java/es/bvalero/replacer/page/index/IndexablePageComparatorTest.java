package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;

class IndexablePageComparatorTest {

    private final IndexablePageComparator indexablePageComparator = new IndexablePageComparator();

    @Test
    void testIndexNewPageWitNoReplacements() {
        int pageId = new Random().nextInt();
        IndexablePage page = IndexablePage
            .builder()
            .id(IndexablePageId.of(WikipediaLanguage.getDefault(), pageId))
            .title("T")
            .replacements(Collections.emptyList())
            .lastUpdate(LocalDate.now())
            .build();

        PageIndexResult result = indexablePageComparator.indexPageReplacements(page, null);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .addPages(Set.of(page))
            .build();
        assertEquals(expected, result);
    }

    @Test
    void testIndexNewPage() {
        IndexablePageId pageId = IndexablePageId.of(WikipediaLanguage.getDefault(), 100);
        IndexableReplacement rep1 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.ofEmpty())
            .position(0)
            .context("")
            .build(); // New => ADD
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(rep1))
            .lastUpdate(LocalDate.now())
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, null);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .addPages(Set.of(page))
            .addReplacements(Set.of(rep1))
            .build();
        assertEquals(expected, toIndex);
    }

    @Test
    void testIndexObsoleteReplacements() {
        IndexablePageId pageId = IndexablePageId.of(WikipediaLanguage.getDefault(), 1);
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(Collections.emptyList())
            .lastUpdate(LocalDate.now())
            .build();

        // Both obsolete to review or reviewed by system ==> Delete
        IndexableReplacement rep2 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.ofEmpty())
            .position(2)
            .context("2")
            .build();
        IndexableReplacement rep3 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.ofEmpty())
            .position(3)
            .context("3")
            .build()
            .setSystemReviewed();
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(rep2, rep3))
            .lastUpdate(LocalDate.now())
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .removeReplacements(Set.of(rep2, rep3))
            .build();

        assertEquals(expected, toIndex);
    }

    @Test
    void testIndexExistingPageSameDate() {
        // R1 : In DB not reviewed => Do nothing
        // R2 : In DB reviewed => Do nothing
        // R5 : Not in DB => Add
        // R6 : Only in DB not reviewed => Delete
        // R7 : Only in DB reviewed by user => Do nothing
        // R8 : Only in DB reviewed by system => Delete

        // Replacements found to index
        IndexablePageId pageId = IndexablePageId.of(WikipediaLanguage.getDefault(), 1);
        IndexableReplacement r1 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .position(1)
            .context("")
            .build();
        IndexableReplacement r2 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "2"))
            .position(2)
            .context("")
            .build();
        IndexableReplacement r5 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "5"))
            .position(5)
            .context("")
            .build();
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1, r2, r5))
            .lastUpdate(LocalDate.now())
            .build();

        // Existing replacements in DB
        IndexableReplacement r1db = r1.withTouched(false); // Trick to clone and match with the one found to index
        IndexableReplacement r2db = r2.setSystemReviewed(); // System is just a normal user here
        IndexableReplacement r6db = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "6"))
            .position(6)
            .context("")
            .build();
        IndexableReplacement r7db = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "7"))
            .position(7)
            .context("")
            .reviewer("")
            .build();
        IndexableReplacement r8db = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "8"))
            .position(8)
            .context("")
            .build()
            .setSystemReviewed();
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1db, r2db, r6db, r7db, r8db))
            .lastUpdate(LocalDate.now())
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .addReplacements(Set.of(r5))
            .removeReplacements(Set.of(r6db, r8db))
            .build();
        assertEquals(expected, toIndex);
    }

    @Test
    void testIndexExistingPageDateAfter() {
        LocalDate same = LocalDate.now();
        LocalDate before = same.minusDays(1);

        // Page: In DB older ==> Update

        // R1 : In DB older not reviewed => Do nothing
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
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .position(1)
            .context("")
            .build();
        IndexableReplacement r2 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "2"))
            .position(2)
            .context("")
            .build();
        IndexableReplacement r3 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "3"))
            .position(3)
            .context("")
            .build();
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1, r2, r3))
            .lastUpdate(same)
            .build();

        // Existing replacements in DB
        IndexableReplacement r1db = r1.withTouched(false); // Trick to clone and match with the one found to index
        IndexableReplacement r2db = r2.withReviewer("");
        IndexableReplacement r4db = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "4"))
            .position(4)
            .context("")
            .build();
        IndexableReplacement r5db = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "5"))
            .position(5)
            .context("")
            .reviewer("")
            .build();
        IndexableReplacement r6db = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "6"))
            .position(6)
            .context("")
            .build()
            .setSystemReviewed();
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1db, r2db, r4db, r5db, r6db))
            .lastUpdate(before)
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .updatePages(Set.of(page))
            .addReplacements(Set.of(r3))
            .removeReplacements(Set.of(r4db, r6db))
            .build();
        assertEquals(expected, toIndex);
    }

    @Test
    void testIndexExistingPageWithNoChanges() {
        LocalDate same = LocalDate.now();

        // R1 : In DB not reviewed => Do nothing

        // Replacements found to index
        IndexablePageId pageId = IndexablePageId.of(WikipediaLanguage.getDefault(), 1);
        IndexableReplacement r1 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .position(1)
            .context("")
            .build();
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1))
            .lastUpdate(same)
            .build();

        // Existing replacements in DB
        IndexableReplacement r1db = r1.withTouched(false); // Trick to clone and match with the one found to index
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1db))
            .lastUpdate(same)
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult.ofEmpty();
        assertEquals(expected, toIndex);
    }

    @Test
    void testIndexExistingPageWithDuplicatedReplacements() {
        LocalDate same = LocalDate.now();

        // Replacements found to index: the same replacement found in 2 different positions with same context
        IndexablePageId pageId = IndexablePageId.of(WikipediaLanguage.getDefault(), 1);
        IndexableReplacement r1 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .position(1)
            .context("C")
            .build();
        IndexableReplacement r2 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .position(5)
            .context("C")
            .build();
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1, r2))
            .lastUpdate(same)
            .build();

        // Existing replacements in DB
        IndexableReplacement r1db = r1.withTouched(false); // Trick to clone and match with the one found to index
        IndexableReplacement r2db = r2.withTouched(false); // Trick to clone and match with the one found to index
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1db, r2db))
            .lastUpdate(same)
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .removeReplacements(Set.of(r2db))
            .build();
        assertEquals(expected, toIndex);
    }

    @Test
    void testIndexDuplicatedDbReplacements() {
        LocalDate same = LocalDate.now();

        // Replacements found to index: the same replacement found in 2 different positions
        IndexablePageId pageId = IndexablePageId.of(WikipediaLanguage.getDefault(), 1);
        IndexableReplacement r1 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .position(1)
            .context("1")
            .build();
        IndexableReplacement r2 = IndexableReplacement
            .builder()
            .indexablePageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .position(5)
            .context("5")
            .build();
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1, r2))
            .lastUpdate(same)
            .build();

        // Existing replacements in DB
        IndexableReplacement r1db = r1.withContext("").withReviewer("X");
        IndexableReplacement r2db = r2.withContext("").withReviewer("X");
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r2db, r1db)) // Force a different order
            .lastUpdate(same)
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        assertEquals(PageIndexResult.ofEmpty(), toIndex);
    }
}
