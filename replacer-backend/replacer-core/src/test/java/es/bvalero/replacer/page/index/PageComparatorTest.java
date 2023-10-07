package es.bvalero.replacer.page.index;

import static es.bvalero.replacer.page.index.PageComparator.toIndexedPage;
import static es.bvalero.replacer.replacement.IndexedReplacement.REVIEWER_SYSTEM;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.replacement.IndexedReplacement;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageComparatorTest {

    private final PageComparator pageComparator = new PageComparator();
    private final LocalDate now = LocalDate.now();
    private final LocalDate before = now.minusDays(1);
    private final IndexablePage page = mock(IndexablePage.class);

    @BeforeEach
    public void setUp() {
        // Mock a complete page
        int pageId = new Random().nextInt();
        when(page.getPageKey()).thenReturn(PageKey.of(WikipediaLanguage.getDefault(), pageId));
        when(page.getTitle()).thenReturn("T");
        when(page.getContent()).thenReturn("123456789");
        when(page.getLastUpdate()).thenReturn(WikipediaTimestamp.now());
    }

    private static Replacement buildFinderReplacement(FinderPage page, int index) {
        return Replacement
            .builder()
            .page(page)
            .start(index)
            .text(String.valueOf(index))
            .type(StandardType.of(ReplacementKind.SIMPLE, String.valueOf(index)))
            .suggestions(List.of(Suggestion.ofNoComment(String.valueOf(index + 1))))
            .build();
    }

    @Test
    void testNewPageWithNoReplacements() {
        Collection<Replacement> replacements = List.of();

        PageComparatorResult result = pageComparator.indexPageReplacements(page, replacements, null);

        PageComparatorResult expected = PageComparatorResult.of(page.getPageKey().getLang());
        expected.addPage(toIndexedPage(page));

        assertEquals(expected, result);
    }

    @Test
    void testNewPageWithReplacements() {
        Replacement r1 = buildFinderReplacement(page, 1); // New => ADD
        Collection<Replacement> replacements = List.of(r1);

        PageComparatorResult toIndex = pageComparator.indexPageReplacements(page, replacements, null);

        PageComparatorResult expected = PageComparatorResult.of(page.getPageKey().getLang());
        expected.addPage(toIndexedPage(page));
        expected.addReplacement(ComparableReplacement.of(r1));
        expected.addReplacementsToReview(replacements);
        expected.addReplacementTypes(List.of(r1.getType()));

        assertEquals(expected, toIndex);
    }

    @Test
    void testExistingPageWithSameDetails() {
        IndexedPage dbPage = IndexedPage
            .builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of())
            .lastUpdate(now)
            .build();

        Collection<Replacement> replacements = List.of();

        PageComparatorResult result = pageComparator.indexPageReplacements(page, replacements, dbPage);

        PageComparatorResult expected = PageComparatorResult.of(page.getPageKey().getLang());
        assertEquals(expected, result);
    }

    @Test
    void testExistingPageWithDateBefore() {
        IndexedPage dbPage = IndexedPage
            .builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of())
            .lastUpdate(before)
            .build();

        Collection<Replacement> replacements = List.of();

        PageComparatorResult result = pageComparator.indexPageReplacements(page, replacements, dbPage);

        PageComparatorResult expected = PageComparatorResult.of(page.getPageKey().getLang());
        expected.updatePage(toIndexedPage(page));

        assertEquals(expected, result);
    }

    @Test
    void testExistingPageWithDateAfter() {
        // We force a difference in the replacements and title to be sure the page is not indexed at all
        when(page.getLastUpdate()).thenReturn(WikipediaTimestamp.of(before.atStartOfDay()));
        IndexedPage dbPage = IndexedPage
            .builder()
            .pageKey(page.getPageKey())
            .title("T2")
            .replacements(List.of())
            .lastUpdate(now)
            .build();

        Replacement r1 = buildFinderReplacement(page, 1);
        Collection<Replacement> replacements = List.of(r1);

        assertThrows(
            IllegalArgumentException.class,
            () -> pageComparator.indexPageReplacements(page, replacements, dbPage)
        );
    }

    @Test
    void testExistingPageWithDifferentTitle() {
        IndexedPage dbPage = IndexedPage
            .builder()
            .pageKey(page.getPageKey())
            .title("T2")
            .replacements(List.of())
            .lastUpdate(now)
            .build();

        Collection<Replacement> replacements = List.of();

        PageComparatorResult result = pageComparator.indexPageReplacements(page, replacements, dbPage);

        PageComparatorResult expected = PageComparatorResult.of(page.getPageKey().getLang());
        expected.updatePage(toIndexedPage(page));

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

        Replacement r1 = buildFinderReplacement(page, 1);
        Replacement r2 = buildFinderReplacement(page, 2);
        Replacement r3 = buildFinderReplacement(page, 3);
        Replacement r4 = buildFinderReplacement(page, 4);
        Replacement r5 = buildFinderReplacement(page, 5);
        Replacement r9 = buildFinderReplacement(page, 9);
        Collection<Replacement> replacements = List.of(r1, r2, r3, r4, r5, r9);

        // Existing replacements in DB
        IndexedReplacement r1db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type((StandardType) r1.getType())
            .start(r1.getStart())
            .context(r1.getContext())
            .build();
        IndexedReplacement r2db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type((StandardType) r2.getType())
            .start(r2.getStart())
            .context(r2.getContext())
            .reviewer("User")
            .build();
        IndexedReplacement r3db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type((StandardType) r3.getType())
            .start(r3.getStart())
            .context(r3.getContext())
            .reviewer(REVIEWER_SYSTEM)
            .build();
        IndexedReplacement r4db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type((StandardType) r4.getType())
            .start(40)
            .context(r4.getContext())
            .build();
        IndexedReplacement r5db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type((StandardType) r5.getType())
            .start(r5.getStart())
            .context("50")
            .build();
        IndexedReplacement r6db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "6"))
            .start(6)
            .context("6")
            .build();
        IndexedReplacement r7db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "7"))
            .start(7)
            .context("7")
            .reviewer("User")
            .build();
        IndexedReplacement r8db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "8"))
            .start(8)
            .context("8")
            .reviewer(REVIEWER_SYSTEM)
            .build();
        IndexedPage dbPage = IndexedPage
            .builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of(r1db, r2db, r3db, r4db, r5db, r6db, r7db, r8db))
            .lastUpdate(now)
            .build();

        PageComparatorResult toIndex = pageComparator.indexPageReplacements(page, replacements, dbPage);

        PageComparatorResult expected = PageComparatorResult.of(page.getPageKey().getLang());
        expected.addReplacement(ComparableReplacement.of(r9));
        expected.updateReplacement(ComparableReplacement.of(r4));
        expected.updateReplacement(ComparableReplacement.of(r5));
        expected.removeReplacement(ComparableReplacement.of(r6db));
        expected.removeReplacement(ComparableReplacement.of(r8db));
        expected.addReplacementsToReview(Set.of(r1, r4, r5, r9));
        expected.addReplacementTypes(List.of(r9.getType()));
        expected.removeReplacementTypes(List.of(r6db.getType()));

        assertEquals(expected, toIndex);
    }

    @Test
    void testIndexExistingPageWithNoChanges() {
        // R1 : In DB not reviewed => Do nothing

        // Replacements found to index
        Replacement r1 = buildFinderReplacement(page, 1);
        Collection<Replacement> replacements = List.of(r1);

        // Existing replacements in DB
        IndexedReplacement r1db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type((StandardType) r1.getType())
            .start(r1.getStart())
            .context(r1.getContext())
            .build();
        IndexedPage dbPage = IndexedPage
            .builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of(r1db))
            .lastUpdate(now)
            .build();

        PageComparatorResult toIndex = pageComparator.indexPageReplacements(page, replacements, dbPage);

        PageComparatorResult expected = PageComparatorResult.of(page.getPageKey().getLang());
        expected.addReplacementsToReview(replacements);

        assertEquals(expected, toIndex);
    }

    @Test
    void testDuplicatedDbReplacementsWithDifferentPosition() {
        Collection<Replacement> replacements = List.of();

        // Existing replacements in DB: the same replacement found in 2 different positions with same context
        IndexedReplacement r1db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .context("C")
            .reviewer("X")
            .build();
        IndexedReplacement r2db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
            .start(5)
            .context("C")
            .reviewer("X")
            .build();
        IndexedPage dbPage = IndexedPage
            .builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of(r1db, r2db))
            .lastUpdate(now)
            .build();

        PageComparatorResult toIndex = pageComparator.indexPageReplacements(page, replacements, dbPage);

        // In fact r1db and r2db are considered the same so any of them could be removed
        assertFalse(toIndex.isEmpty());
        assertEquals(1, toIndex.getRemoveReplacements().size());
    }

    @Test
    void testDuplicatedDbReplacementsWithDifferentPositionFarEnough() {
        Collection<Replacement> replacements = List.of();

        // Existing replacements in DB: the same replacement found in 2 different positions with same context
        // We force a distance between the replacements so even having the same context they are not considered the same
        IndexedReplacement r1db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .context("C")
            .reviewer("X")
            .build();
        IndexedReplacement r2db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
            .start(100)
            .context("C")
            .reviewer("X")
            .build();
        IndexedPage dbPage = IndexedPage
            .builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of(r1db, r2db))
            .lastUpdate(now)
            .build();

        PageComparatorResult toIndex = pageComparator.indexPageReplacements(page, replacements, dbPage);

        // In this case r1db and r2db are not considered the same so none of them is removed
        assertTrue(toIndex.isEmpty());
    }

    @Test
    void testDuplicatedDbReplacementsWithDifferentContext() {
        Replacement r1 = buildFinderReplacement(page, 1);
        Collection<Replacement> replacements = List.of(r1);

        // Existing replacements in DB: the same replacement found in the same position with different context
        // but one is not reviewed matching with the found one in the page
        IndexedReplacement r1db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type((StandardType) r1.getType())
            .start(r1.getStart())
            .context("1")
            .build();
        IndexedReplacement r2db = IndexedReplacement
            .builder()
            .pageKey(page.getPageKey())
            .type((StandardType) r1.getType())
            .start(r1.getStart())
            .context("")
            .reviewer("X")
            .build();
        IndexedPage dbPage = IndexedPage
            .builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .replacements(List.of(r2db, r1db)) // Force a different order
            .lastUpdate(now)
            .build();

        PageComparatorResult toIndex = pageComparator.indexPageReplacements(page, replacements, dbPage);

        PageComparatorResult expected = PageComparatorResult.of(page.getPageKey().getLang());
        // In fact r1db and r2db are considered the same so any of them could be removed
        // In this case we remove the one not reviewed
        expected.removeReplacement(ComparableReplacement.of(r1db));
        expected.removeReplacementTypes(List.of(r1db.getType()));

        assertEquals(expected, toIndex);
    }
}
