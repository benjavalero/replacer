package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;

class IndexablePageComparatorTest {

    private final IndexablePageComparator indexablePageComparator = new IndexablePageComparator();
    private final LocalDate now = LocalDate.now();
    private final LocalDate before = now.minusDays(1);

    @Test
    void testNewPageWithNoReplacements() {
        int pageId = new Random().nextInt();
        IndexablePage page = IndexablePage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId))
            .title("T")
            .replacements(Collections.emptyList())
            .lastUpdate(now)
            .build();

        PageIndexResult result = indexablePageComparator.indexPageReplacements(page, null);

        PageIndexResult expected = PageIndexResult.builder().status(PageIndexStatus.PAGE_INDEXED).addPage(page).build();
        assertEquals(expected, result);
    }

    @Test
    void testNewPageWithReplacements() {
        int id = new Random().nextInt();
        WikipediaPageId pageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), id);
        IndexableReplacement r1 = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .context("1")
            .build(); // New => ADD
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1))
            .lastUpdate(now)
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, null);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .addPage(page)
            .addReplacement(r1)
            .build();
        assertEquals(expected, toIndex);
    }

    @Test
    void testExistingPageWithSameDetails() {
        int pageId = new Random().nextInt();
        IndexablePage page = IndexablePage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId))
            .title("T")
            .replacements(Collections.emptyList())
            .lastUpdate(now)
            .build();
        IndexablePage dbPage = page.toBuilder().build();

        PageIndexResult result = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult.ofEmpty();
        assertEquals(expected, result);
    }

    @Test
    void testExistingPageWithDateBefore() {
        int pageId = new Random().nextInt();
        IndexablePage page = IndexablePage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId))
            .title("T")
            .replacements(Collections.emptyList())
            .lastUpdate(now)
            .build();
        IndexablePage dbPage = page.toBuilder().lastUpdate(before).build();

        PageIndexResult result = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .updatePage(page)
            .build();
        assertEquals(expected, result);
    }

    @Test
    void testExistingPageWithDateAfter() {
        int id = new Random().nextInt();
        WikipediaPageId pageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), id);
        IndexableReplacement r1 = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .context("1")
            .build();
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1))
            .lastUpdate(before)
            .build();
        // We force a difference in the replacements and title to be sure the page is not indexed at all
        IndexablePage dbPage = page
            .toBuilder()
            .title("T2")
            .replacements(Collections.emptyList())
            .lastUpdate(now)
            .build();

        PageIndexResult result = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXABLE).build();
        assertEquals(expected, result);
    }

    @Test
    void testExistingPageWithDifferentTitle() {
        int pageId = new Random().nextInt();
        IndexablePage page = IndexablePage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId))
            .title("T")
            .replacements(Collections.emptyList())
            .lastUpdate(now)
            .build();
        IndexablePage dbPage = page.toBuilder().title("T2").build();

        PageIndexResult result = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .updatePage(page)
            .build();
        assertEquals(expected, result);
    }

    @Test
    void testExistingPageWithDifferentReplacements() {
        // Existing
        // R1 : In DB not reviewed with same details => Do nothing
        // R2 : In DB reviewed by a user => Do nothing
        // R3 : In DB reviewed by system => Do nothing
        // R4 : In DB not reviewed with different position => Update
        // R5 : In DB not reviewed with different context => Update

        // Only in DB
        // R6 : Only in DB not reviewed => Delete
        // R7 : Only in DB reviewed by user => Do nothing
        // R8 : Only in DB reviewed by system => Delete

        // New
        // R9 : Not in DB => Add

        int id = new Random().nextInt();
        WikipediaPageId pageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), id);
        IndexableReplacement r1 = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .context("1")
            .build();
        IndexableReplacement r2 = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "2"))
            .start(2)
            .context("2")
            .build();
        IndexableReplacement r3 = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "3"))
            .start(3)
            .context("3")
            .build();
        IndexableReplacement r4 = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "4"))
            .start(4)
            .context("4")
            .build();
        IndexableReplacement r5 = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "5"))
            .start(5)
            .context("5")
            .build();
        IndexableReplacement r9 = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "9"))
            .start(9)
            .context("9")
            .build();
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1, r2, r3, r4, r5, r9))
            .lastUpdate(now)
            .build();

        // Existing replacements in DB
        IndexableReplacement r1db = r1.withTouched(false); // Trick to clone and match with the one found to index
        IndexableReplacement r2db = r2.withReviewer("User");
        IndexableReplacement r3db = r3.setSystemReviewed();
        IndexableReplacement r4db = r4.withStart(40);
        IndexableReplacement r5db = r5.withContext("50");
        IndexableReplacement r6db = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "6"))
            .start(6)
            .context("6")
            .build();
        IndexableReplacement r7db = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "7"))
            .start(7)
            .context("7")
            .reviewer("User")
            .build();
        IndexableReplacement r8db = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "8"))
            .start(8)
            .context("8")
            .build()
            .setSystemReviewed();
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1db, r2db, r3db, r4db, r5db, r6db, r7db, r8db))
            .lastUpdate(now)
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            .addReplacements(Set.of(r9))
            .updateReplacements(Set.of(r4, r5))
            .removeReplacements(Set.of(r6db, r8db))
            .build();
        assertEquals(expected, toIndex);
    }

    @Test
    void testIndexExistingPageWithNoChanges() {
        // R1 : In DB not reviewed => Do nothing

        // Replacements found to index
        WikipediaPageId pageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), 1);
        IndexableReplacement r1 = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .context("1")
            .build();
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1))
            .lastUpdate(now)
            .build();

        // Existing replacements in DB
        IndexableReplacement r1db = r1.withTouched(false); // Trick to clone and match with the one found to index
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1db))
            .lastUpdate(now)
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult.ofEmpty();
        assertEquals(expected, toIndex);
    }

    @Test
    void testDuplicatedDbReplacementsWithDifferentPosition() {
        WikipediaPageId pageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), 1);
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(Collections.emptyList())
            .lastUpdate(now)
            .build();

        // Existing replacements in DB: the same replacement found in 2 different positions with same context
        IndexableReplacement r1db = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .context("C")
            .reviewer("X")
            .build();
        IndexableReplacement r2db = r1db.withStart(5);
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1db, r2db))
            .lastUpdate(now)
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        // In fact r1db and r2db are considered the same so any of them could be removed
        assertEquals(PageIndexStatus.PAGE_INDEXED, toIndex.getStatus());
        assertEquals(1, toIndex.getRemoveReplacements().size());
    }

    @Test
    void testDuplicatedDbReplacementsWithDifferentContext() {
        WikipediaPageId pageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), 1);
        IndexableReplacement r1 = IndexableReplacement
            .builder()
            .pageId(pageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .context("1")
            .build();
        IndexablePage page = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r1))
            .lastUpdate(now)
            .build();

        // Existing replacements in DB: the same replacement found in the same position with different context
        // but one is not reviewed matching with the found one in the page
        IndexableReplacement r1db = r1.withContext("1");
        IndexableReplacement r2db = r1db.withContext("").withReviewer("X");
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(pageId)
            .title("T")
            .replacements(List.of(r2db, r1db)) // Force a different order
            .lastUpdate(now)
            .build();

        PageIndexResult toIndex = indexablePageComparator.indexPageReplacements(page, dbPage);

        PageIndexResult expected = PageIndexResult
            .builder()
            .status(PageIndexStatus.PAGE_INDEXED)
            // In fact r1db and r2db are considered the same so any of them could be removed
            // In this case we remove the first one which is not reviewed
            .removeReplacements(Set.of(r1db))
            .build();
        assertEquals(expected, toIndex);
    }
}
