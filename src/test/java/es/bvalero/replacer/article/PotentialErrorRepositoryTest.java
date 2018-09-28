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
public class PotentialErrorRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private PotentialErrorRepository potentialErrorRepository;

    @Test
    public void testFindMisspellingsGrouped() {
        Article article1 = new Article(1, "");
        Article article2 = new Article(2, "");
        Article article3 = new Article(3, "");
        articleRepository.saveAll(Arrays.asList(article1, article2, article3));

        PotentialError error1 = new PotentialError(article1, PotentialErrorType.MISSPELLING, "aber");
        PotentialError error2 = new PotentialError(article2, PotentialErrorType.MISSPELLING, "aber");
        PotentialError error3 = new PotentialError(article2, PotentialErrorType.MISSPELLING, "madrid");
        PotentialError error4 = new PotentialError(article3, PotentialErrorType.MISSPELLING, "paris");
        potentialErrorRepository.saveAll(Arrays.asList(error1, error2, error3, error4));

        Assert.assertEquals(3, potentialErrorRepository.findMisspellingsGrouped().size());
    }

    @Test
    public void testRandomArticleByWord() {
        Article article1 = new Article(1, "");
        Article article2 = new Article(2, "");
        Article article3 = new Article(3, "");
        articleRepository.saveAll(Arrays.asList(article1, article2, article3));

        PotentialError error1 = new PotentialError(article1, PotentialErrorType.MISSPELLING, "aber");
        PotentialError error2 = new PotentialError(article2, PotentialErrorType.MISSPELLING, "aber");
        PotentialError error3 = new PotentialError(article3, PotentialErrorType.MISSPELLING, "aber");
        potentialErrorRepository.saveAll(Arrays.asList(error1, error2, error3));

        Assert.assertTrue(potentialErrorRepository
                .findRandomByWord("xxx", PageRequest.of(0, 1))
                .isEmpty());

        Assert.assertEquals(3, potentialErrorRepository
                .findRandomByWord("aber", PageRequest.of(0, 3))
                .size());
    }

}
