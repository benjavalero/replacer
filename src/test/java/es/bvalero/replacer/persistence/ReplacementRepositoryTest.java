package es.bvalero.replacer.persistence;

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
    private ArticleRepository articleRepository;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Test
    public void testFindMisspellingsGrouped() {
        Article article1 = new Article.ArticleBuilder().setId(1).setTitle("1").build();
        Article article2 = new Article.ArticleBuilder().setId(2).setTitle("2").build();
        Article article3 = new Article.ArticleBuilder().setId(3).setTitle("3").build();
        articleRepository.saveAll(Arrays.asList(article1, article2, article3));

        Replacement error1 = new Replacement.ReplacementBuilder()
                .setArticle(article1)
                .setType(ReplacementType.MISSPELLING)
                .setText("aber")
                .build();
        Replacement error2 = new Replacement.ReplacementBuilder()
                .setArticle(article2)
                .setType(ReplacementType.MISSPELLING)
                .setText("aber")
                .build();
        Replacement error3 = new Replacement.ReplacementBuilder()
                .setArticle(article2)
                .setType(ReplacementType.MISSPELLING)
                .setText("madrid")
                .build();
        Replacement error4 = new Replacement.ReplacementBuilder()
                .setArticle(article3)
                .setType(ReplacementType.MISSPELLING)
                .setText("paris")
                .build();
        replacementRepository.saveAll(Arrays.asList(error1, error2, error3, error4));

        Assert.assertEquals(3, replacementRepository.findMisspellingsGrouped().size());
    }

    @Test
    public void testRandomArticleByWord() {
        Article article1 = new Article.ArticleBuilder().setId(1).setTitle("1").build();
        Article article2 = new Article.ArticleBuilder().setId(2).setTitle("2").build();
        Article article3 = new Article.ArticleBuilder().setId(3).setTitle("3").build();
        articleRepository.saveAll(Arrays.asList(article1, article2, article3));

        Replacement error1 = new Replacement.ReplacementBuilder()
                .setArticle(article1)
                .setType(ReplacementType.MISSPELLING)
                .setText("aber")
                .build();
        Replacement error2 = new Replacement.ReplacementBuilder()
                .setArticle(article2)
                .setType(ReplacementType.MISSPELLING)
                .setText("aber")
                .build();
        Replacement error3 = new Replacement.ReplacementBuilder()
                .setArticle(article3)
                .setType(ReplacementType.MISSPELLING)
                .setText("aber")
                .build();
        replacementRepository.saveAll(Arrays.asList(error1, error2, error3));

        Assert.assertTrue(replacementRepository
                .findRandomByWord("xxx", PageRequest.of(0, 1))
                .isEmpty());

        Assert.assertEquals(3, replacementRepository
                .findRandomByWord("aber", PageRequest.of(0, 3))
                .size());
    }

}
