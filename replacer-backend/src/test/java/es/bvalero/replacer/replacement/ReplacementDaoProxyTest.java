package es.bvalero.replacer.replacement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

        when(replacementStatsDao.countReplacementsReviewed(any(WikipediaLanguage.class))).thenReturn(count);

        assertEquals(count, replacementDaoProxy.countReplacementsReviewed(WikipediaLanguage.SPANISH));
    }

    @Test
    void testCountReplacementsToReview() {
        long count = new Random().nextLong();

        when(replacementStatsDao.countReplacementsNotReviewed(any(WikipediaLanguage.class))).thenReturn(count);

        assertEquals(count, replacementDaoProxy.countReplacementsNotReviewed(WikipediaLanguage.SPANISH));
    }

    @Test
    void testCountReplacementsGroupedByReviewer() {
        List<ReviewerCount> result = new ArrayList<>();

        when(replacementStatsDao.countReplacementsGroupedByReviewer(any(WikipediaLanguage.class))).thenReturn(result);

        assertEquals(result, replacementDaoProxy.countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH));
    }

    @Test
    void testGetCachedReplacementCount() throws ReplacerException {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();

        TypeSubtypeCount count1 = TypeSubtypeCount.of("X", "Y", 2L);
        TypeSubtypeCount count2 = TypeSubtypeCount.of("X", "Z", 1L);
        List<TypeSubtypeCount> counts = Arrays.asList(count1, count2);
        when(replacementStatsDao.countReplacementsGroupedByType(lang)).thenReturn(LanguageCount.build(counts));

        replacementDaoProxy.scheduledUpdateReplacementCount();

        LanguageCount langCount = replacementDaoProxy.countReplacementsGroupedByType(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains("X"));
        assertEquals(2, langCount.get("X").size());
        assertEquals(2L, langCount.get("X").get("Y").get().getCount());
        assertEquals(1L, langCount.get("X").get("Z").get().getCount());

        // Decrease a replacement count
        replacementDaoProxy.decrementSubtypeCount(lang, "X", "Y");

        langCount = replacementDaoProxy.countReplacementsGroupedByType(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains("X"));
        assertEquals(2, langCount.get("X").size());
        assertEquals(1L, langCount.get("X").get("Y").get().getCount());
        assertEquals(1L, langCount.get("X").get("Z").get().getCount());

        // Decrease a replacement count emptying it
        replacementDaoProxy.decrementSubtypeCount(lang, "X", "Z");

        langCount = replacementDaoProxy.countReplacementsGroupedByType(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains("X"));
        assertEquals(1, langCount.get("X").size());
        assertEquals(1L, langCount.get("X").get("Y").get().getCount());
        assertTrue(langCount.get("X").get("Z").isEmpty());

        // Remove a replacement count not existing in cache
        replacementDaoProxy.removeCachedReplacementCount(lang, "A", "B");

        langCount = replacementDaoProxy.countReplacementsGroupedByType(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains("X"));
        assertEquals(1, langCount.get("X").size());

        // Remove a replacement count existing in cache
        replacementDaoProxy.removeCachedReplacementCount(lang, "X", "Y");

        langCount = replacementDaoProxy.countReplacementsGroupedByType(lang);
        assertTrue(langCount.isEmpty());
    }
}
