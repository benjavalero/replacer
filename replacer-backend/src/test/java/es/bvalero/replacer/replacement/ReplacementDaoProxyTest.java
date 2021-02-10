package es.bvalero.replacer.replacement;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class ReplacementDaoProxyTest {

    @Mock
    private ReplacementStatsDao replacementStatsDao;

    @InjectMocks
    private ReplacementDaoProxy replacementDaoProxy;

    @BeforeEach
    public void setUp() {
        replacementDaoProxy = new ReplacementDaoProxy();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testCountReplacementsReviewed() {
        long count = new Random().nextLong();

        Mockito
            .when(replacementStatsDao.countReplacementsReviewed(Mockito.any(WikipediaLanguage.class)))
            .thenReturn(count);

        Assertions.assertEquals(count, replacementDaoProxy.countReplacementsReviewed(WikipediaLanguage.SPANISH));
    }

    @Test
    void testCountReplacementsToReview() {
        long count = new Random().nextLong();

        Mockito
            .when(replacementStatsDao.countReplacementsNotReviewed(Mockito.any(WikipediaLanguage.class)))
            .thenReturn(count);

        Assertions.assertEquals(count, replacementDaoProxy.countReplacementsNotReviewed(WikipediaLanguage.SPANISH));
    }

    @Test
    void testCountReplacementsGroupedByReviewer() {
        List<ReviewerCount> result = new ArrayList<>();

        Mockito
            .when(replacementStatsDao.countReplacementsGroupedByReviewer(Mockito.any(WikipediaLanguage.class)))
            .thenReturn(result);

        Assertions.assertEquals(
            result,
            replacementDaoProxy.countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH)
        );
    }

    @Test
    void testGetCachedReplacementCount() throws ReplacerException {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();

        TypeSubtypeCount count1 = TypeSubtypeCount.of("X", "Y", 2L);
        TypeSubtypeCount count2 = TypeSubtypeCount.of("X", "Z", 1L);
        List<TypeSubtypeCount> counts = Arrays.asList(count1, count2);
        Mockito.when(replacementStatsDao.countReplacementsGroupedByType(lang)).thenReturn(LanguageCount.build(counts));

        replacementDaoProxy.scheduledUpdateReplacementCount();

        LanguageCount langCount = replacementDaoProxy.countReplacementsGroupedByType(lang);
        Assertions.assertEquals(1, langCount.size());
        Assertions.assertTrue(langCount.contains("X"));
        Assertions.assertEquals(2, langCount.get("X").size());
        Assertions.assertEquals(2L, langCount.get("X").get("Y").get().getCount());
        Assertions.assertEquals(1L, langCount.get("X").get("Z").get().getCount());

        // Decrease a replacement count
        replacementDaoProxy.decrementSubtypeCount(lang, "X", "Y");

        langCount = replacementDaoProxy.countReplacementsGroupedByType(lang);
        Assertions.assertEquals(1, langCount.size());
        Assertions.assertTrue(langCount.contains("X"));
        Assertions.assertEquals(2, langCount.get("X").size());
        Assertions.assertEquals(1L, langCount.get("X").get("Y").get().getCount());
        Assertions.assertEquals(1L, langCount.get("X").get("Z").get().getCount());

        // Decrease a replacement count emptying it
        replacementDaoProxy.decrementSubtypeCount(lang, "X", "Z");

        langCount = replacementDaoProxy.countReplacementsGroupedByType(lang);
        Assertions.assertEquals(1, langCount.size());
        Assertions.assertTrue(langCount.contains("X"));
        Assertions.assertEquals(1, langCount.get("X").size());
        Assertions.assertEquals(1L, langCount.get("X").get("Y").get().getCount());
        Assertions.assertTrue(langCount.get("X").get("Z").isEmpty());

        // Remove a replacement count not existing in cache
        replacementDaoProxy.removeCachedReplacementCount(lang, "A", "B");

        langCount = replacementDaoProxy.countReplacementsGroupedByType(lang);
        Assertions.assertEquals(1, langCount.size());
        Assertions.assertTrue(langCount.contains("X"));
        Assertions.assertEquals(1, langCount.get("X").size());

        // Remove a replacement count existing in cache
        replacementDaoProxy.removeCachedReplacementCount(lang, "X", "Y");

        langCount = replacementDaoProxy.countReplacementsGroupedByType(lang);
        Assertions.assertTrue(langCount.isEmpty());
    }
}
