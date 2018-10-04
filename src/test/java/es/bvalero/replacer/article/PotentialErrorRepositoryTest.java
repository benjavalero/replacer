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
        Article article1 = new Article.ArticleBuilder().setId(1).setTitle("").build();
        Article article2 = new Article.ArticleBuilder().setId(2).setTitle("").build();
        Article article3 = new Article.ArticleBuilder().setId(3).setTitle("").build();
        articleRepository.saveAll(Arrays.asList(article1, article2, article3));

        PotentialError error1 = new PotentialError.PotentialErrorBuilder()
                .setArticle(article1)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("aber")
                .build();
        PotentialError error2 = new PotentialError.PotentialErrorBuilder()
                .setArticle(article2)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("aber")
                .build();
        PotentialError error3 = new PotentialError.PotentialErrorBuilder()
                .setArticle(article2)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("madrid")
                .build();
        PotentialError error4 = new PotentialError.PotentialErrorBuilder()
                .setArticle(article3)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("paris")
                .build();
        potentialErrorRepository.saveAll(Arrays.asList(error1, error2, error3, error4));

        Assert.assertEquals(3, potentialErrorRepository.findMisspellingsGrouped().size());
    }

    @Test
    public void testRandomArticleByWord() {
        Article article1 = new Article.ArticleBuilder().setId(1).setTitle("").build();
        Article article2 = new Article.ArticleBuilder().setId(2).setTitle("").build();
        Article article3 = new Article.ArticleBuilder().setId(3).setTitle("").build();
        articleRepository.saveAll(Arrays.asList(article1, article2, article3));

        PotentialError error1 = new PotentialError.PotentialErrorBuilder()
                .setArticle(article1)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("aber")
                .build();
        PotentialError error2 = new PotentialError.PotentialErrorBuilder()
                .setArticle(article2)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("aber")
                .build();
        PotentialError error3 = new PotentialError.PotentialErrorBuilder()
                .setArticle(article3)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("aber")
                .build();
        potentialErrorRepository.saveAll(Arrays.asList(error1, error2, error3));

        Assert.assertTrue(potentialErrorRepository
                .findRandomByWord("xxx", PageRequest.of(0, 1))
                .isEmpty());

        Assert.assertEquals(3, potentialErrorRepository
                .findRandomByWord("aber", PageRequest.of(0, 3))
                .size());
    }

}
