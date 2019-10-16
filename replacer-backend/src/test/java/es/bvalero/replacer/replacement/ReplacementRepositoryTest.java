package es.bvalero.replacer.replacement;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ReplacementRepositoryTest {

    @Autowired
    private ReplacementRepository replacementRepository;

    @Test
    public void testFindReplacementsGrouped() {
        ReplacementEntity error1 = new ReplacementEntity(1, "X", "aber", 1);
        ReplacementEntity error2 = new ReplacementEntity(2, "X", "aber", 2);
        ReplacementEntity error3 = new ReplacementEntity(2, "X", "madrid", 3);
        ReplacementEntity error4 = new ReplacementEntity(3, "X", "paris", 4);
        ReplacementEntity error5 = new ReplacementEntity(3, "X", "habia", 5);
        error5.setReviewer("x");
        replacementRepository.saveAll(Arrays.asList(error1, error2, error3, error4, error5));

        Assert.assertEquals(3, replacementRepository.countGroupedByTypeAndSubtype().size());
    }

    @Test
    public void testRandomArticleByWord() {
        ReplacementEntity error1 = new ReplacementEntity(1, "X", "aber", 1);
        ReplacementEntity error2 = new ReplacementEntity(2, "X", "aber", 2);
        ReplacementEntity error3 = new ReplacementEntity(3, "X", "aber", 3);
        replacementRepository.saveAll(Arrays.asList(error1, error2, error3));

        Assert.assertTrue(replacementRepository.findRandomArticleIdsToReviewByTypeAndSubtype(
                "xxx", "zzz", PageRequest.of(0, 1))
                .isEmpty());

        Assert.assertEquals(3, replacementRepository.findRandomArticleIdsToReviewByTypeAndSubtype(
                "X", "aber", PageRequest.of(0, 3))
                .size());
    }

}
