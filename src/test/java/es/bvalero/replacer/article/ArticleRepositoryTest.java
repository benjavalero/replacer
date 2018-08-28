package es.bvalero.replacer.article;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    public void testSaveAndFind() {
        PotentialError error1 = new PotentialError(PotentialErrorType.MISSPELLING, "A");
        PotentialError error2 = new PotentialError(PotentialErrorType.MISSPELLING, "B");
        Article newArticle = new Article(1, "Andorra");
        newArticle.addPotentialError(error1);
        newArticle.addPotentialError(error2);
        articleRepository.save(newArticle);

        Assert.assertNull(articleRepository.findOne(0));

        Article article = articleRepository.findOne(1);
        Assert.assertNotNull(article);
        Assert.assertEquals("Andorra", article.getTitle());
        Assert.assertEquals(2, article.getPotentialErrors().size());
    }

    @Test
    public void testFindNotReviewed() {
        Article article1 = new Article(1, "");
        Article article2 = new Article(2, "");
        article2.setReviewDate(new Timestamp(new Date().getTime()));
        Article article3 = new Article(3, "");
        articleRepository.save(Arrays.asList(article1, article2, article3));

        List<Article> notReviewedArticles = articleRepository.findRandomByReviewDateNull(new PageRequest(0, 3));
        Assert.assertNotNull(notReviewedArticles);
        Assert.assertEquals(2, notReviewedArticles.size());
    }

    @Test
    public void testSetArticleAsReviewed() {
        Article newArticle = new Article(1, "");
        articleRepository.save(newArticle);
        Assert.assertNull(articleRepository.findOne(1).getReviewDate());

        articleRepository.setArticleAsReviewed(1);
        Assert.assertNotNull(articleRepository.findOne(1).getReviewDate());
        Assert.assertFalse(articleRepository.findOne(1).getReviewDate().after(new Date()));
    }

    @Test
    public void testDelete() {
        Article newArticle = new Article(1, "");
        articleRepository.save(newArticle);
        Assert.assertNull(articleRepository.findOne(1).getReviewDate());

        articleRepository.delete(1);
        Assert.assertNull(articleRepository.findOne(1));
    }

    @Test
    public void testCountByReviewDate() {
        Article article1 = new Article(1, "");
        Article article2 = new Article(2, "");
        article2.setReviewDate(new Timestamp(new Date().getTime()));
        Article article3 = new Article(3, "");
        articleRepository.save(Arrays.asList(article1, article2, article3));

        Assert.assertEquals(1, articleRepository.countByReviewDateNotNull().longValue());
        Assert.assertEquals(2, articleRepository.countByReviewDateNull().longValue());
    }

}
