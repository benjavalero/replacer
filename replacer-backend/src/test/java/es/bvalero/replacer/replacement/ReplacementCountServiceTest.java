package es.bvalero.replacer.replacement;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ReplacementCountServiceTest {

    @Mock
    private ReplacementRepository replacementRepository;

    @InjectMocks
    private ReplacementCountService replacementCountService;

    @Before
    public void setUp() {
        replacementCountService = new ReplacementCountService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCountReplacements() {
        long count = new Random().nextLong();

        Mockito.when(replacementRepository.countByReviewerIsNullOrReviewerIsNot(Mockito.anyString()))
                .thenReturn(count);

        Assert.assertEquals(count, replacementCountService.countAllReplacements());
    }

    @Test
    public void testCountReplacementsReviewed() {
        long count = new Random().nextLong();

        Mockito.when(replacementRepository.countByReviewerIsNotNullAndReviewerIsNot(Mockito.anyString()))
                .thenReturn(count);

        Assert.assertEquals(count, replacementCountService.countReplacementsReviewed());
    }

    @Test
    public void testCountReplacementsToReview() {
        long count = new Random().nextLong();

        Mockito.when(replacementRepository.countByReviewerIsNull())
                .thenReturn(count);

        Assert.assertEquals(count, replacementCountService.countReplacementsToReview());
    }

    @Test
    public void testCountReplacementsGroupedByReviewer() {
        List<ReviewerCount> result = new ArrayList<>();

        Mockito.when(replacementRepository.countGroupedByReviewer(Mockito.anyString()))
                .thenReturn(result);

        Assert.assertEquals(result, replacementCountService.countReplacementsGroupedByReviewer());
    }

    @Test
    public void testGetCachedReplacementCount() {
        // At start the count is zero
        Assert.assertTrue(replacementCountService.getCachedReplacementCount().isEmpty());

        TypeSubtypeCount count1 = new TypeSubtypeCount("X", "Y", 2L);
        TypeSubtypeCount count2 = new TypeSubtypeCount("X", "Z", 1L);
        List<TypeSubtypeCount> counts = Arrays.asList(count1, count2);
        Mockito.when(replacementRepository.countGroupedByTypeAndSubtype())
                .thenReturn(counts);

        replacementCountService.updateReplacementCount();

        List<TypeCount> typeCounts = replacementCountService.findReplacementCount();
        Assert.assertEquals(1, typeCounts.size());
        Assert.assertEquals("X", typeCounts.get(0).getType());
        Assert.assertEquals(2, typeCounts.get(0).getSubtypeCounts().size());
        Assert.assertTrue(typeCounts.get(0).getSubtypeCounts().contains(SubtypeCount.of("Y", 2L)));
        Assert.assertTrue(typeCounts.get(0).getSubtypeCounts().contains(SubtypeCount.of("Z", 1L)));

        Assert.assertFalse(replacementCountService.getCachedReplacementCount().isEmpty());
        Assert.assertEquals(1, replacementCountService.getCachedReplacementCount().size());
        Assert.assertEquals(2, replacementCountService.getCachedReplacementCount().get("X").size());

        // Decrease a replacement count
        replacementCountService.decreaseCachedReplacementsCount("X", "Y", 1);

        Assert.assertFalse(replacementCountService.getCachedReplacementCount().isEmpty());
        Assert.assertEquals(1, replacementCountService.getCachedReplacementCount().size());
        Assert.assertEquals(2, replacementCountService.getCachedReplacementCount().get("X").size());
        Assert.assertEquals(Long.valueOf(1), replacementCountService.getCachedReplacementCount().get("X").get("Y"));

        // Decrease a replacement count emptying it
        replacementCountService.decreaseCachedReplacementsCount("X", "Z", 1);

        Assert.assertFalse(replacementCountService.getCachedReplacementCount().isEmpty());
        Assert.assertEquals(1, replacementCountService.getCachedReplacementCount().size());
        Assert.assertEquals(1, replacementCountService.getCachedReplacementCount().get("X").size());
        Assert.assertFalse(replacementCountService.getCachedReplacementCount().get("X").containsKey("Z"));

        // Remove a replacement count not existing in cache
        replacementCountService.removeCachedReplacementCount("A", "B");

        Assert.assertFalse(replacementCountService.getCachedReplacementCount().isEmpty());
        Assert.assertEquals(1, replacementCountService.getCachedReplacementCount().size());
        Assert.assertEquals(1, replacementCountService.getCachedReplacementCount().get("X").size());

        // Remove a replacement count existing in cache
        replacementCountService.removeCachedReplacementCount("X", "Y");

        Assert.assertTrue(replacementCountService.getCachedReplacementCount().isEmpty());
    }

}
