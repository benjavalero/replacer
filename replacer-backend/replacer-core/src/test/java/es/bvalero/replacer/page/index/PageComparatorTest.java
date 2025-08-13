package es.bvalero.replacer.page.index;

import static es.bvalero.replacer.page.index.PageComparator.toIndexedPage;
import static es.bvalero.replacer.replacement.IndexedReplacement.REVIEWER_SYSTEM;
import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.save.IndexedPageStatus;
import es.bvalero.replacer.replacement.IndexedReplacement;
import es.bvalero.replacer.replacement.save.IndexedReplacementStatus;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.junit.jupiter.api.Test;

class PageComparatorTest {

    private final PageComparator pageComparator = new PageComparator();
    private final int pageId = new Random().nextInt();
    private final LocalDate now = LocalDate.now();
    private final LocalDate before = now.minusDays(1);
    private final IndexablePage page = IndexablePage.builder()
        .pageKey(PageKey.of(WikipediaLanguage.getDefault(), pageId))
        .namespace(WikipediaNamespace.getDefault().getValue())
        .title("T")
        .content("123456789")
        .lastUpdate(WikipediaTimestamp.now().toString())
        .build();

    private static Replacement buildFinderReplacement(IndexablePage indexablePage, int index) {
        FinderPage page = indexablePage.toFinderPage();
        return Replacement.of(
            index,
            String.valueOf(index),
            StandardType.of(ReplacementKind.SIMPLE, String.valueOf(index)),
            List.of(Suggestion.ofNoComment(String.valueOf(index + 1))),
            page.getContent()
        );
    }

    @Test
    void testNewPageWithNoReplacements() {
        Collection<Replacement> replacements = List.of();

        IndexedPage result = pageComparator.indexPageReplacements(page, replacements, null);

        IndexedPage expected = toIndexedPage(page, IndexedPageStatus.ADD);

        assertTrue(IndexedPage.compare(expected, result));
    }

    @Test
    void testNewPageWithReplacements() {
        Replacement r1 = buildFinderReplacement(page, 1); // New => ADD
        Collection<Replacement> replacements = List.of(r1);

        IndexedPage result = pageComparator.indexPageReplacements(page, replacements, null);

        IndexedPage expected = toIndexedPage(page, IndexedPageStatus.ADD);
        expected.addReplacement(ComparableReplacement.of(r1, page.getPageKey()).toDomain(IndexedReplacementStatus.ADD));

        assertTrue(IndexedPage.compare(expected, result));
    }

    @Test
    void testExistingPageWithSameDetails() {
        IndexedPage dbPage = IndexedPage.builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of())
            .lastUpdate(now)
            .build();

        Collection<Replacement> replacements = List.of();

        IndexedPage result = pageComparator.indexPageReplacements(page, replacements, dbPage);

        IndexedPage expected = toIndexedPage(page, IndexedPageStatus.INDEXED);

        assertTrue(IndexedPage.compare(expected, result));
    }

    @Test
    void testExistingPageWithDateBefore() {
        IndexedPage dbPage = IndexedPage.builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of())
            .lastUpdate(before)
            .build();

        Collection<Replacement> replacements = List.of();

        IndexedPage result = pageComparator.indexPageReplacements(page, replacements, dbPage);

        IndexedPage expected = toIndexedPage(page, IndexedPageStatus.UPDATE);

        assertTrue(IndexedPage.compare(expected, result));
    }

    @Test
    void testExistingPageWithDifferentTitle() {
        IndexedPage dbPage = IndexedPage.builder()
            .pageKey(page.getPageKey())
            .title("T2")
            .replacements(List.of())
            .lastUpdate(now)
            .build();

        Collection<Replacement> replacements = List.of();

        IndexedPage result = pageComparator.indexPageReplacements(page, replacements, dbPage);

        IndexedPage expected = toIndexedPage(page, IndexedPageStatus.UPDATE);

        assertTrue(IndexedPage.compare(expected, result));
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

        Replacement r1 = buildFinderReplacement(page, 1);
        Replacement r2 = buildFinderReplacement(page, 2);
        Replacement r3 = buildFinderReplacement(page, 3);
        Replacement r4 = buildFinderReplacement(page, 4);
        Replacement r5 = buildFinderReplacement(page, 5);
        Replacement r9 = buildFinderReplacement(page, 9);
        Collection<Replacement> replacements = List.of(r1, r2, r3, r4, r5, r9);

        // Existing replacements in DB
        IndexedReplacement r1db = IndexedReplacement.builder()
            .id(1)
            .pageKey(page.getPageKey())
            .type((StandardType) r1.getType())
            .start(r1.getStart())
            .context(r1.getContext())
            .build();
        IndexedReplacement r2db = IndexedReplacement.builder()
            .id(2)
            .pageKey(page.getPageKey())
            .type((StandardType) r2.getType())
            .start(r2.getStart())
            .context(r2.getContext())
            .reviewer("User")
            .build();
        IndexedReplacement r3db = IndexedReplacement.builder()
            .id(3)
            .pageKey(page.getPageKey())
            .type((StandardType) r3.getType())
            .start(r3.getStart())
            .context(r3.getContext())
            .reviewer(REVIEWER_SYSTEM)
            .build();
        IndexedReplacement r4db = IndexedReplacement.builder()
            .id(4)
            .pageKey(page.getPageKey())
            .type((StandardType) r4.getType())
            .start(5)
            .context(r4.getContext())
            .build();
        IndexedReplacement r5db = IndexedReplacement.builder()
            .id(5)
            .pageKey(page.getPageKey())
            .type((StandardType) r5.getType())
            .start(r5.getStart())
            .context("50")
            .build();
        IndexedReplacement r6db = IndexedReplacement.builder()
            .id(6)
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "6"))
            .start(6)
            .context("6")
            .build();
        IndexedReplacement r7db = IndexedReplacement.builder()
            .id(7)
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "7"))
            .start(7)
            .context("7")
            .reviewer("User")
            .build();
        IndexedReplacement r8db = IndexedReplacement.builder()
            .id(8)
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "8"))
            .start(8)
            .context("8")
            .reviewer(REVIEWER_SYSTEM)
            .build();
        IndexedPage dbPage = IndexedPage.builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of(r1db, r2db, r3db, r4db, r5db, r6db, r7db, r8db))
            .lastUpdate(now)
            .build();

        IndexedPage result = pageComparator.indexPageReplacements(page, replacements, dbPage);

        IndexedPage expected = toIndexedPage(page, IndexedPageStatus.INDEXED);
        expected.addReplacement(ComparableReplacement.of(r9, page.getPageKey()).toDomain(IndexedReplacementStatus.ADD));
        expected.addReplacement(
            ComparableReplacement.of(r4, page.getPageKey()).toDomain(4, IndexedReplacementStatus.UPDATE)
        );
        expected.addReplacement(
            ComparableReplacement.of(r5, page.getPageKey()).toDomain(5, IndexedReplacementStatus.UPDATE)
        );
        expected.addReplacement(ComparableReplacement.of(r6db).toDomain(IndexedReplacementStatus.REMOVE));
        expected.addReplacement(ComparableReplacement.of(r8db).toDomain(IndexedReplacementStatus.REMOVE));
        expected.addReplacement(
            ComparableReplacement.of(r1, page.getPageKey()).toDomain(1, IndexedReplacementStatus.INDEXED)
        );

        assertTrue(
            result
                .getReplacements()
                .stream()
                .map(IndexedReplacement::getStatus)
                .noneMatch(s -> s == IndexedReplacementStatus.UNDEFINED)
        );
        assertTrue(
            result
                .getReplacements()
                .stream()
                .filter(r -> r.getStatus() != IndexedReplacementStatus.ADD)
                .allMatch(r -> Objects.nonNull(r.getId()))
        );
        assertTrue(IndexedPage.compare(expected, result));
    }

    @Test
    void testIndexExistingPageWithNoChanges() {
        // R1 : In DB not reviewed => Do nothing

        // Replacements found to index
        Replacement r1 = buildFinderReplacement(page, 1);
        Collection<Replacement> replacements = List.of(r1);

        // Existing replacements in DB
        IndexedReplacement r1db = IndexedReplacement.builder()
            .id(1)
            .pageKey(page.getPageKey())
            .type((StandardType) r1.getType())
            .start(r1.getStart())
            .context(r1.getContext())
            .build();
        IndexedPage dbPage = IndexedPage.builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of(r1db))
            .lastUpdate(now)
            .build();

        IndexedPage result = pageComparator.indexPageReplacements(page, replacements, dbPage);

        IndexedPage expected = toIndexedPage(page, IndexedPageStatus.INDEXED);
        expected.addReplacement(
            ComparableReplacement.of(r1, page.getPageKey()).toDomain(1, IndexedReplacementStatus.INDEXED)
        );

        assertTrue(IndexedPage.compare(expected, result));
    }

    @Test
    void testDuplicatedDbReplacementsWithDifferentPosition() {
        Collection<Replacement> replacements = List.of();

        // Existing replacements in DB: the same replacement found in 2 different positions with same context
        String context = "1234567890";
        IndexedReplacement r1db = IndexedReplacement.builder()
            .id(1)
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .context(context)
            .reviewer("X")
            .build();
        IndexedReplacement r2db = IndexedReplacement.builder()
            .id(2)
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
            .start(5)
            .context(context)
            .reviewer("X")
            .build();
        IndexedPage dbPage = IndexedPage.builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of(r1db, r2db))
            .lastUpdate(now)
            .build();

        IndexedPage toIndex = pageComparator.indexPageReplacements(page, replacements, dbPage);

        // In fact r1db and r2db are considered the same so any of them could be removed
        assertFalse(toIndex.getReplacements().isEmpty());
        assertEquals(
            1,
            toIndex.getReplacements().stream().filter(ir -> ir.getStatus() == IndexedReplacementStatus.REMOVE).count()
        );
    }

    @Test
    void testDuplicatedDbReplacementsWithDifferentPositionFarEnough() {
        Collection<Replacement> replacements = List.of();

        // Existing replacements in DB: the same replacement found in 2 different positions with same context
        // We force a distance between the replacements so even having the same context they are not considered the same
        IndexedReplacement r1db = IndexedReplacement.builder()
            .id(1)
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .context("C")
            .reviewer("X")
            .build();
        IndexedReplacement r2db = IndexedReplacement.builder()
            .id(2)
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
            .start(100)
            .context("C")
            .reviewer("X")
            .build();
        IndexedPage dbPage = IndexedPage.builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of(r1db, r2db))
            .lastUpdate(now)
            .build();

        IndexedPage toIndex = pageComparator.indexPageReplacements(page, replacements, dbPage);

        // In this case r1db and r2db are not considered the same so none of them is removed
        assertTrue(
            toIndex.getReplacements().stream().noneMatch(ir -> ir.getStatus() == IndexedReplacementStatus.REMOVE)
        );
    }

    @Test
    void testDuplicatedDbReplacementsWithDifferentContext() {
        Replacement r1 = buildFinderReplacement(page, 1);
        Collection<Replacement> replacements = List.of(r1);

        // Existing replacements in DB: the same replacement found in the same position with different context
        // but one is not reviewed matching with the found one in the page
        IndexedReplacement r1db = IndexedReplacement.builder()
            .id(1)
            .pageKey(page.getPageKey())
            .type((StandardType) r1.getType())
            .start(r1.getStart())
            .context("1")
            .build();
        IndexedReplacement r2db = IndexedReplacement.builder()
            .id(2)
            .pageKey(page.getPageKey())
            .type((StandardType) r1.getType())
            .start(r1.getStart())
            .context("")
            .reviewer("X")
            .build();
        IndexedPage dbPage = IndexedPage.builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of(r2db, r1db)) // Force a different order
            .lastUpdate(now)
            .build();

        IndexedPage result = pageComparator.indexPageReplacements(page, replacements, dbPage);

        IndexedPage expected = toIndexedPage(page, IndexedPageStatus.INDEXED);
        // In fact r1db and r2db are considered the same so any of them could be removed
        // In this case we remove the one not reviewed
        expected.addReplacement(ComparableReplacement.of(r1db).toDomain(IndexedReplacementStatus.REMOVE));

        assertTrue(IndexedPage.compare(expected, result));
    }

    @Test
    void testComparableReplacementNotCloseEnough() {
        String text =
            "abcde" +
            "12345678902134567890" +
            "words" +
            "12345678901234567890" +
            "other" +
            "12345678902134567890" +
            "words" +
            "12345678901234567890" +
            "xyz";
        IndexablePage page = this.page.withContent(text);

        Replacement r1 = Replacement.of(
            25,
            "words",
            StandardType.of(ReplacementKind.SIMPLE, "words"),
            List.of(Suggestion.ofNoComment("Words")),
            page.getContent()
        );
        ComparableReplacement cr1 = ComparableReplacement.of(r1, page.getPageKey());
        Replacement r2 = Replacement.of(
            75,
            "words",
            StandardType.of(ReplacementKind.SIMPLE, "words"),
            List.of(Suggestion.ofNoComment("Words")),
            page.getContent()
        );
        ComparableReplacement cr2 = ComparableReplacement.of(r2, page.getPageKey());

        assertFalse(cr1.isContextCloseEnough(cr2));
        assertFalse(cr2.isContextCloseEnough(cr1));
    }

    @Test
    void testComparableReplacementCloseEnough() {
        String text =
            "abcde" +
            "12345678902134567890" +
            "words" +
            "12345678901234567890" +
            "12345678902134567890" +
            "words" +
            "12345678901234567890" +
            "xyz";
        IndexablePage page = this.page.withContent(text);

        Replacement r1 = Replacement.of(
            25,
            "words",
            StandardType.of(ReplacementKind.SIMPLE, "words"),
            List.of(Suggestion.ofNoComment("Words")),
            page.getContent()
        );
        ComparableReplacement cr1 = ComparableReplacement.of(r1, page.getPageKey());
        Replacement r2 = Replacement.of(
            70,
            "words",
            StandardType.of(ReplacementKind.SIMPLE, "words"),
            List.of(Suggestion.ofNoComment("Words")),
            page.getContent()
        );
        ComparableReplacement cr2 = ComparableReplacement.of(r2, page.getPageKey());

        assertTrue(cr1.isContextCloseEnough(cr2));
        assertTrue(cr2.isContextCloseEnough(cr1));
    }
}
