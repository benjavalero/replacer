package es.bvalero.replacer.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.wikipedia.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReviewSectionFinderTest {

    @Mock
    private WikipediaPageRepository wikipediaPageRepository;

    @InjectMocks
    private ReviewSectionFinder reviewSectionFinder;

    @BeforeEach
    public void setUp() {
        reviewSectionFinder = new ReviewSectionFinder();
        MockitoAnnotations.openMocks(this);
    }

    private WikipediaPage buildWikipediaPage(int pageId, String content) {
        return WikipediaPage
            .builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), pageId))
            .namespace(WikipediaNamespace.getDefault())
            .title("T")
            .content(content)
            .lastUpdate(WikipediaTimestamp.now())
            .queryTimestamp(WikipediaTimestamp.now())
            .build();
    }

    @Test
    void testFindSectionReviewNoSections() {
        WikipediaPage page = buildWikipediaPage(1, "Any content");
        List<Replacement> replacements = Collections.emptyList();

        when(wikipediaPageRepository.findSectionsInPage(any(PageKey.class))).thenReturn(Collections.emptyList());

        Review review = Review.of(page, null, replacements, 1);
        Optional<Review> sectionReview = reviewSectionFinder.findPageReviewSection(review);

        assertFalse(sectionReview.isPresent());
    }

    @Test
    void testFindSectionReview() {
        int pageId = 1;
        String content = "This is an sample content.";
        Suggestion suggestion = Suggestion.ofNoComment("a");
        Replacement replacement = Replacement
            .builder()
            .start(8)
            .text("an")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "an"))
            .suggestions(Collections.singletonList(suggestion))
            .build();
        List<Replacement> replacements = Collections.singletonList(replacement);
        WikipediaPage page = buildWikipediaPage(pageId, content);
        int numPending = 10;
        Review pageReview = Review.of(page, null, replacements, numPending);

        int sectionId = 3;
        int offset = 5;
        WikipediaSection section = WikipediaSection
            .builder()
            .level(2)
            .index(sectionId)
            .byteOffset(offset)
            .anchor("X")
            .build();
        when(wikipediaPageRepository.findSectionsInPage(page.getPageKey()))
            .thenReturn(Collections.singletonList(section));

        String sectionContent = content.substring(offset, 10);
        WikipediaPage pageSection = WikipediaPage
            .builder()
            .pageKey(page.getPageKey())
            .namespace(page.getNamespace())
            .title(page.getTitle())
            .content(sectionContent)
            .lastUpdate(page.getLastUpdate())
            .queryTimestamp(page.getQueryTimestamp())
            .build();
        when(wikipediaPageRepository.findPageSection(page.getPageKey(), section)).thenReturn(Optional.of(pageSection));

        Optional<Review> sectionReview = reviewSectionFinder.findPageReviewSection(pageReview);

        assertTrue(sectionReview.isPresent());
        sectionReview.ifPresent(review -> {
            assertEquals(page.getPageKey(), review.getPage().getPageKey());
            assertNotNull(review.getSection());
            assertEquals(section, review.getSection());
            assertEquals(sectionContent, review.getPage().getContent());
            assertEquals(1, review.getReplacements().size());
            assertEquals(8 - offset, new ArrayList<>(review.getReplacements()).get(0).getStart());
            assertEquals(numPending, review.getNumPending());
        });
    }
}
