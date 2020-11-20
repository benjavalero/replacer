package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class ReplacementCountServiceTest {
    @Mock
    private ReplacementDao replacementDao;

    @InjectMocks
    private ReplacementCountService replacementCountService;

    @BeforeEach
    public void setUp() {
        replacementCountService = new ReplacementCountService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testCountReplacementsReviewed() {
        long count = new Random().nextLong();

        Mockito.when(replacementDao.countReplacementsReviewed(Mockito.any(WikipediaLanguage.class))).thenReturn(count);

        Assertions.assertEquals(count, replacementCountService.countReplacementsReviewed(WikipediaLanguage.SPANISH));
    }

    @Test
    void testCountReplacementsToReview() {
        long count = new Random().nextLong();

        Mockito
            .when(replacementDao.countReplacementsNotReviewed(Mockito.any(WikipediaLanguage.class)))
            .thenReturn(count);

        Assertions.assertEquals(count, replacementCountService.countReplacementsNotReviewed(WikipediaLanguage.SPANISH));
    }

    @Test
    void testCountReplacementsGroupedByReviewer() {
        List<ReviewerCount> result = new ArrayList<>();

        Mockito
            .when(replacementDao.countReplacementsGroupedByReviewer(Mockito.any(WikipediaLanguage.class)))
            .thenReturn(result);

        Assertions.assertEquals(
            result,
            replacementCountService.countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH)
        );
    }

    @Test
    void testGetCachedReplacementCount() {
        WikipediaLanguage lang = WikipediaLanguage.SPANISH;
        String langCode = lang.getCode();

        // At start the count is zero
        Assertions.assertTrue(replacementCountService.getCachedReplacementTypeCounts(lang).isEmpty());

        TypeSubtypeCount count1 = new TypeSubtypeCount(langCode, "X", "Y", 2L);
        TypeSubtypeCount count2 = new TypeSubtypeCount(langCode, "X", "Z", 1L);
        List<TypeSubtypeCount> counts = Arrays.asList(count1, count2);
        Mockito.when(replacementDao.countPagesGroupedByTypeAndSubtype()).thenReturn(counts);

        replacementCountService.scheduledUpdateReplacementTypeCounts();

        List<TypeCount> typeCounts = replacementCountService.getCachedReplacementTypeCounts(lang);
        Assertions.assertEquals(1, typeCounts.size());
        Assertions.assertEquals("X", typeCounts.get(0).getType());
        Assertions.assertEquals(2, typeCounts.get(0).getSubtypeCounts().size());
        Assertions.assertEquals(2L, typeCounts.get(0).get("Y").get().getCount());
        Assertions.assertEquals(1L, typeCounts.get(0).get("Z").get().getCount());

        Assertions.assertFalse(replacementCountService.getCachedReplacementTypeCounts(lang).isEmpty());
        Assertions.assertEquals(1, replacementCountService.getCachedReplacementTypeCounts(lang).size());
        Assertions.assertEquals(
            2,
            replacementCountService
                .getCachedReplacementTypeCounts(lang)
                .stream()
                .filter(t -> t.getType().equals("X"))
                .findAny()
                .get()
                .getSubtypeCounts()
                .size()
        );

        // Decrease a replacement count
        replacementCountService.decreaseCachedReplacementsCount(lang, "X", "Y", 1);

        Assertions.assertFalse(replacementCountService.getCachedReplacementTypeCounts(lang).isEmpty());
        Assertions.assertEquals(1, replacementCountService.getCachedReplacementTypeCounts(lang).size());
        Assertions.assertEquals(
            2,
            replacementCountService
                .getCachedReplacementTypeCounts(lang)
                .stream()
                .filter(t -> t.getType().equals("X"))
                .findAny()
                .get()
                .getSubtypeCounts()
                .size()
        );
        Assertions.assertEquals(
            1L,
            replacementCountService
                .getCachedReplacementTypeCounts(lang)
                .stream()
                .filter(t -> t.getType().equals("X"))
                .findAny()
                .get()
                .get("Y")
                .get()
                .getCount()
        );

        // Decrease a replacement count emptying it
        replacementCountService.decreaseCachedReplacementsCount(lang, "X", "Z", 1);

        Assertions.assertFalse(replacementCountService.getCachedReplacementTypeCounts(lang).isEmpty());
        Assertions.assertEquals(1, replacementCountService.getCachedReplacementTypeCounts(lang).size());
        Assertions.assertEquals(
            1,
            replacementCountService
                .getCachedReplacementTypeCounts(lang)
                .stream()
                .filter(t -> t.getType().equals("X"))
                .findAny()
                .get()
                .getSubtypeCounts()
                .size()
        );
        Assertions.assertFalse(
            replacementCountService
                .getCachedReplacementTypeCounts(lang)
                .stream()
                .filter(t -> t.getType().equals("X"))
                .findAny()
                .get()
                .get("Z")
                .isPresent()
        );

        // Remove a replacement count not existing in cache
        replacementCountService.removeCachedReplacementCount(lang, "A", "B");

        Assertions.assertFalse(replacementCountService.getCachedReplacementTypeCounts(lang).isEmpty());
        Assertions.assertEquals(1, replacementCountService.getCachedReplacementTypeCounts(lang).size());
        Assertions.assertEquals(
            1,
            replacementCountService
                .getCachedReplacementTypeCounts(lang)
                .stream()
                .filter(t -> t.getType().equals("X"))
                .findAny()
                .get()
                .getSubtypeCounts()
                .size()
        );

        // Remove a replacement count existing in cache
        replacementCountService.removeCachedReplacementCount(lang, "X", "Y");

        Assertions.assertTrue(replacementCountService.getCachedReplacementTypeCounts(lang).isEmpty());
    }
}
