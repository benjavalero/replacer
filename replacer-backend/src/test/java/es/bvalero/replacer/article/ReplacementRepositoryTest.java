package es.bvalero.replacer.article;

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
        Replacement error1 = new Replacement(1, "X", "aber", 1);
        Replacement error2 = new Replacement(2, "X", "aber", 2);
        Replacement error3 = new Replacement(2, "X", "madrid", 3);
        Replacement error4 = new Replacement(3, "X", "paris", 4);
        Replacement error5 = new Replacement(3, "X", "habia", 5).withReviewer("x");
        replacementRepository.saveAll(Arrays.asList(error1, error2, error3, error4, error5));

        Assert.assertEquals(3, replacementRepository.findReplacementCountByTypeAndSubtype().size());
    }

    @Test
    public void testRandomArticleByWord() {
        Replacement error1 = new Replacement(1, "X", "aber", 1);
        Replacement error2 = new Replacement(2, "X", "aber", 2);
        Replacement error3 = new Replacement(3, "X", "aber", 3);
        replacementRepository.saveAll(Arrays.asList(error1, error2, error3));

        Assert.assertTrue(replacementRepository.findRandomArticleIdsToReviewByTypeAndSubtype(
                "xxx", "zzz", PageRequest.of(0, 1))
                .isEmpty());

        Assert.assertEquals(3, replacementRepository.findRandomArticleIdsToReviewByTypeAndSubtype(
                "X", "aber", PageRequest.of(0, 3))
                .size());
    }

}
