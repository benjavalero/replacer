package es.bvalero.replacer.article;

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

public class ArticleStatsServiceTest {

    @Mock
    private ReplacementRepository replacementRepository;

    @InjectMocks
    private ArticleStatsService articleStatsService;

    @Before
    public void setUp() {
        articleStatsService = new ArticleStatsService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCountReplacements() {
        long count = new Random().nextLong();

        Mockito.when(replacementRepository.countByReviewerIsNullOrReviewerIsNot(Mockito.anyString()))
                .thenReturn(count);

        Assert.assertEquals(count, articleStatsService.countReplacements());
    }

    @Test
    public void testCountReplacementsReviewed() {
        long count = new Random().nextLong();

        Mockito.when(replacementRepository.countByReviewerIsNotNullAndReviewerIsNot(Mockito.anyString()))
                .thenReturn(count);

        Assert.assertEquals(count, articleStatsService.countReplacementsReviewed());
    }

    @Test
    public void testCountReplacementsToReview() {
        long count = new Random().nextLong();

        Mockito.when(replacementRepository.countByReviewerIsNull())
                .thenReturn(count);

        Assert.assertEquals(count, articleStatsService.countReplacementsToReview());
    }

    @Test
    public void testCountReplacementsGroupedByReviewer() {
        List<Object[]> result = new ArrayList<>();

        Mockito.when(replacementRepository.countGroupedByReviewer(Mockito.anyString()))
                .thenReturn(result);

        Assert.assertEquals(result, articleStatsService.countReplacementsGroupedByReviewer());
    }

    @Test
    public void testFindMisspellingsGrouped() {
        // At start the count is zero
        Assert.assertTrue(articleStatsService.findMisspellingsGrouped().isEmpty());

        ReplacementCount count1 = new ReplacementCount("X", "Y", 2L);
        ReplacementCount count2 = new ReplacementCount("X", "Z", 1L);
        List<ReplacementCount> counts = Arrays.asList(count1, count2);
        Mockito.when(replacementRepository.findReplacementCountByTypeAndSubtype())
                .thenReturn(counts);

        articleStatsService.updateReplacementCount();

        Assert.assertFalse(articleStatsService.findMisspellingsGrouped().isEmpty());
        Assert.assertEquals(2, articleStatsService.findMisspellingsGrouped().size());
        Assert.assertEquals(counts, articleStatsService.findMisspellingsGrouped().get("X"));

        // Decrease a replacement count
        articleStatsService.decreaseCachedReplacementsCount("X", "Y", 1);

        Assert.assertFalse(articleStatsService.findMisspellingsGrouped().isEmpty());
        Assert.assertEquals(2, articleStatsService.findMisspellingsGrouped().size());
        Assert.assertEquals(2, articleStatsService.findMisspellingsGrouped().get("X").size());
        Assert.assertEquals(Long.valueOf(1), articleStatsService.findMisspellingsGrouped().get("X").stream()
                .filter(c -> c.getSubtype().equals("Y")).map(ReplacementCount::getCount).findAny().orElse(0L));

        // Decrease a replacement count emptying it
        articleStatsService.decreaseCachedReplacementsCount("X", "Z", 1);

        Assert.assertFalse(articleStatsService.findMisspellingsGrouped().isEmpty());
        Assert.assertEquals(1, articleStatsService.findMisspellingsGrouped().size());
        Assert.assertEquals(1, articleStatsService.findMisspellingsGrouped().get("X").size());
        Assert.assertTrue(articleStatsService.findMisspellingsGrouped().get("X").stream()
                .noneMatch(c -> c.getSubtype().equals("Z")));

        // Remove a replacement count not existing in cache
        articleStatsService.removeCachedReplacementCount("A", "B");

        Assert.assertFalse(articleStatsService.findMisspellingsGrouped().isEmpty());
        Assert.assertEquals(1, articleStatsService.findMisspellingsGrouped().size());
        Assert.assertEquals(1, articleStatsService.findMisspellingsGrouped().get("X").size());

        // Remove a replacement count existing in cache
        articleStatsService.removeCachedReplacementCount("X", "Y");

        Assert.assertTrue(articleStatsService.findMisspellingsGrouped().isEmpty());
    }

}
