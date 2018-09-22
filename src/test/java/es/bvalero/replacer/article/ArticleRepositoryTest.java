package es.bvalero.replacer.article;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private PotentialErrorRepository potentialErrorRepository;

    @Test
    public void testInsert() {
        Assert.assertEquals(0, articleRepository.count());

        Article newArticle = new Article(1, "Andorra");
        articleRepository.save(newArticle);

        Assert.assertEquals(1, articleRepository.count());
        Optional<Article> dbArticle = articleRepository.findById(1);
        Assert.assertTrue(dbArticle.isPresent());
        dbArticle.ifPresent(article -> Assert.assertEquals(newArticle, article));

        // By default Addition Date is the current one
        dbArticle.ifPresent(article -> Assert.assertNotNull(article.getAdditionDate()));
    }

    @Test
    public void testInsertDuplicated() {
        Assert.assertEquals(0, articleRepository.count());

        Article newArticle = new Article(1, "Andorra");
        articleRepository.save(newArticle);

        String title = "España";
        Article duplicated = new Article(1, title);
        articleRepository.save(duplicated);

        // The second insert updates the first
        Assert.assertEquals(1, articleRepository.count());
        Optional<Article> dbArticle = articleRepository.findById(1);
        Assert.assertTrue(dbArticle.isPresent());
        dbArticle.ifPresent(article -> Assert.assertEquals(title, article.getTitle()));
    }

    @Test
    public void testInsertWithReplacements() {
        Assert.assertEquals(0, articleRepository.count());

        Article newArticle = new Article(1, "Andorra");
        newArticle.addPotentialError(new PotentialError(PotentialErrorType.MISSPELLING, "A"));
        newArticle.addPotentialError(new PotentialError(PotentialErrorType.MISSPELLING, "B"));
        articleRepository.save(newArticle);

        Assert.assertEquals(1, articleRepository.count());
        Optional<Article> dbArticle = articleRepository.findById(1);
        Assert.assertTrue(dbArticle.isPresent());
        dbArticle.ifPresent(article -> Assert.assertEquals(2, article.getPotentialErrors().size()));
        Assert.assertEquals(2, potentialErrorRepository.count());
    }

    @Test
    public void testModifyArticle() {
        Assert.assertEquals(0, articleRepository.count());

        Article newArticle = new Article(1, "Andorra");
        articleRepository.save(newArticle);

        articleRepository.findById(1).ifPresent(
                article -> Assert.assertNull(article.getReviewDate()));

        // Modify attributes
        String newTitle = "España";
        Timestamp newAdditionDate = new Timestamp(System.currentTimeMillis());
        Timestamp newReviewDate = new Timestamp(System.currentTimeMillis());
        newArticle.setTitle(newTitle);
        newArticle.setAdditionDate(newAdditionDate);
        newArticle.setReviewDate(newReviewDate);
        articleRepository.save(newArticle);

        Assert.assertEquals(1, articleRepository.count());
        articleRepository.findById(1).ifPresent(article -> {
            Assert.assertEquals(newTitle, article.getTitle());
            Assert.assertEquals(newAdditionDate, article.getAdditionDate());
            Assert.assertEquals(newReviewDate, article.getReviewDate());
        });
    }

    @Test
    public void testModifyReplacementList() {
        Assert.assertEquals(0, articleRepository.count());

        Article newArticle = new Article(1, "Andorra");
        newArticle.addPotentialError(new PotentialError(PotentialErrorType.MISSPELLING, "A"));
        newArticle.addPotentialError(new PotentialError(PotentialErrorType.MISSPELLING, "B"));
        newArticle.addPotentialError(new PotentialError(PotentialErrorType.MISSPELLING, "C"));
        articleRepository.save(newArticle);

        Assert.assertEquals(1, articleRepository.count());
        articleRepository.findById(1).ifPresent(article ->
                Assert.assertEquals(3, article.getPotentialErrors().size()));
        Assert.assertEquals(3, potentialErrorRepository.count());

        // Delete replacements
        newArticle.getPotentialErrors().remove(2);
        newArticle.getPotentialErrors().remove(0);
        articleRepository.save(newArticle);
        // TODO This method deletes all the replacements and then adds one again

        articleRepository.findById(1).ifPresent(article -> {
            Assert.assertEquals(1, article.getPotentialErrors().size());
            Assert.assertEquals("B", article.getPotentialErrors().get(0).getText());
        });
        Assert.assertEquals(1, potentialErrorRepository.count());

        // Add replacements
        newArticle.getPotentialErrors().add(new PotentialError(PotentialErrorType.MISSPELLING, "D"));
        articleRepository.save(newArticle);
        // TODO This method also deletes all the replacements and then adds one again

        articleRepository.findById(1).ifPresent(article -> {
            Assert.assertEquals(2, article.getPotentialErrors().size());
            Assert.assertEquals("B", article.getPotentialErrors().get(0).getText());
            Assert.assertEquals("D", article.getPotentialErrors().get(1).getText());
        });
        Assert.assertEquals(2, potentialErrorRepository.count());
    }

}
