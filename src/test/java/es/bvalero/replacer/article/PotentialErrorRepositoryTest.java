package es.bvalero.replacer.article;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PotentialErrorRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private PotentialErrorRepository potentialErrorRepository;

    @Test
    public void testCountNotReviewed() {
        Article article1 = new Article(1, "");
        Article article2 = new Article(2, "");
        Article article3 = new Article(3, "");
        articleRepository.save(Arrays.asList(article1, article2, article3));

        PotentialError error1 = new PotentialError(PotentialErrorType.MISSPELLING, "aber");
        PotentialError error2 = new PotentialError(PotentialErrorType.MISSPELLING, "aber");
        PotentialError error3 = new PotentialError(PotentialErrorType.MISSPELLING, "madrid");
        PotentialError error4 = new PotentialError(PotentialErrorType.MISSPELLING, "paris");
        article1.addPotentialError(error1);
        article2.addPotentialError(error2);
        article2.addPotentialError(error3);
        article3.addPotentialError(error4);
        potentialErrorRepository.save(Arrays.asList(error1, error2, error3, error4));

        Assert.assertEquals(4, potentialErrorRepository.countNotReviewed().longValue());

        article2.setReviewDate(new Timestamp(new Date().getTime()));
        articleRepository.save(article2);

        Assert.assertEquals(2, potentialErrorRepository.countNotReviewed().longValue());
    }

    @Test
    public void testFindMisspellingsGrouped() {
        Article article1 = new Article(1, "");
        Article article2 = new Article(2, "");
        Article article3 = new Article(3, "");
        articleRepository.save(Arrays.asList(article1, article2, article3));

        PotentialError error1 = new PotentialError(PotentialErrorType.MISSPELLING, "aber");
        PotentialError error2 = new PotentialError(PotentialErrorType.MISSPELLING, "aber");
        PotentialError error3 = new PotentialError(PotentialErrorType.MISSPELLING, "madrid");
        PotentialError error4 = new PotentialError(PotentialErrorType.MISSPELLING, "paris");
        article1.addPotentialError(error1);
        article2.addPotentialError(error2);
        article2.addPotentialError(error3);
        article3.addPotentialError(error4);
        potentialErrorRepository.save(Arrays.asList(error1, error2, error3, error4));

        Assert.assertEquals(3, potentialErrorRepository.findMisspellingsGrouped().size());

        article2.setReviewDate(new Timestamp(new Date().getTime()));
        articleRepository.save(article2);

        Assert.assertEquals(2, potentialErrorRepository.findMisspellingsGrouped().size());
    }

}
