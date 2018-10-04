package es.bvalero.replacer.article;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
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

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);

        String title = "España";
        Article duplicated = new Article.ArticleBuilder().setId(1).setTitle(title).build();
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

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        PotentialError replacement1 = new PotentialError.PotentialErrorBuilder()
                .setArticle(newArticle)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("A").build();
        PotentialError replacement2 = new PotentialError.PotentialErrorBuilder()
                .setArticle(newArticle)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("B")
                .build();
        potentialErrorRepository.saveAll(Arrays.asList(replacement1, replacement2));

        Assert.assertEquals(1, articleRepository.count());
        Assert.assertEquals(2, potentialErrorRepository.count());
        Assert.assertEquals(2, potentialErrorRepository.findByArticle(newArticle).size());
    }

    @Test
    public void testModifyArticle() {
        Assert.assertEquals(0, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);

        articleRepository.findById(1).ifPresent(
                article -> Assert.assertNull(article.getReviewDate()));

        // Modify attributes
        String newTitle = "España";
        LocalDateTime newAdditionDate = LocalDateTime.now();
        LocalDateTime newReviewDate = LocalDateTime.now();
        Article toSave = newArticle
                .withTitle(newTitle)
                .withAdditionDate(newAdditionDate)
                .withReviewDate(newReviewDate);
        articleRepository.save(toSave);

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

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        PotentialError replacement1 = new PotentialError.PotentialErrorBuilder()
                .setArticle(newArticle)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("A")
                .build();
        PotentialError replacement2 = new PotentialError.PotentialErrorBuilder()
                .setArticle(newArticle)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("B")
                .build();
        PotentialError replacement3 = new PotentialError.PotentialErrorBuilder()
                .setArticle(newArticle)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("C")
                .build();
        potentialErrorRepository.saveAll(Arrays.asList(replacement1, replacement2, replacement3));

        Assert.assertEquals(1, articleRepository.count());
        Assert.assertEquals(3, potentialErrorRepository.count());
        Assert.assertEquals(3, potentialErrorRepository.findByArticle(newArticle).size());

        // Delete replacements
        potentialErrorRepository.deleteInBatch(Arrays.asList(replacement1, replacement3));

        Assert.assertEquals(1, articleRepository.count());
        Assert.assertEquals(1, potentialErrorRepository.count());
        Assert.assertEquals(1, potentialErrorRepository.findByArticle(newArticle).size());
        Assert.assertEquals("B", potentialErrorRepository.findByArticle(newArticle).get(0).getText());

        // Add replacements
        PotentialError replacement4 = new PotentialError.PotentialErrorBuilder()
                .setArticle(newArticle)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("D")
                .build();
        potentialErrorRepository.save(replacement4);

        Assert.assertEquals(1, articleRepository.count());
        Assert.assertEquals(2, potentialErrorRepository.count());
        Assert.assertEquals(2, potentialErrorRepository.findByArticle(newArticle).size());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testInsertDuplicatedReplacement() {
        Assert.assertEquals(0, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        PotentialError replacement1 = new PotentialError.PotentialErrorBuilder()
                .setArticle(newArticle)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("A")
                .build();
        PotentialError replacement2 = new PotentialError.PotentialErrorBuilder()
                .setArticle(newArticle)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("A")
                .build();
        potentialErrorRepository.saveAll(Arrays.asList(replacement1, replacement2));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testDeleteArticleWithReplacements() {
        Assert.assertEquals(0, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        PotentialError replacement1 = new PotentialError.PotentialErrorBuilder()
                .setArticle(newArticle)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("A")
                .build();
        PotentialError replacement2 = new PotentialError.PotentialErrorBuilder()
                .setArticle(newArticle)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("B")
                .build();
        potentialErrorRepository.saveAll(Arrays.asList(replacement1, replacement2));

        Assert.assertEquals(1, articleRepository.count());
        Assert.assertEquals(2, potentialErrorRepository.count());

        // This will fail
        // We have removed partially the relation between the entities but we keep the FK in Replacement
        articleRepository.delete(newArticle);
        Assert.assertEquals(0, articleRepository.count());
    }

    @Test
    public void testDeleteArticleInCascade() {
        Assert.assertEquals(0, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        PotentialError replacement1 = new PotentialError.PotentialErrorBuilder()
                .setArticle(newArticle)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("A")
                .build();
        PotentialError replacement2 = new PotentialError.PotentialErrorBuilder()
                .setArticle(newArticle)
                .setType(PotentialErrorType.MISSPELLING)
                .setText("B")
                .build();
        potentialErrorRepository.saveAll(Arrays.asList(replacement1, replacement2));

        Assert.assertEquals(1, articleRepository.count());
        Assert.assertEquals(2, potentialErrorRepository.count());

        potentialErrorRepository.deleteByArticle(newArticle);
        articleRepository.delete(newArticle);
        Assert.assertEquals(0, articleRepository.count());
        Assert.assertEquals(0, potentialErrorRepository.count());
    }

    @Test
    @Ignore
    public void testPerformance() {
        List<Article> articles = new ArrayList<>(50);
        List<PotentialError> replacements = new ArrayList<>(500);
        for (int i = 0; i < 1000000; i++) {
            Article newArticle = new Article.ArticleBuilder().setId(i).setTitle("Title" + String.valueOf(i)).build();
            for (int j = 0; j < 10; j++) {
                PotentialError replacement = new PotentialError.PotentialErrorBuilder()
                        .setArticle(newArticle)
                        .setType(PotentialErrorType.MISSPELLING)
                        .setText("Text" + String.valueOf(j))
                        .build();
                replacements.add(replacement);
            }
            articles.add(newArticle);
            if (articles.size() == 50) {
                articleRepository.saveAll(articles);
                articles.clear();
                potentialErrorRepository.saveAll(replacements);
                replacements.clear();

                articleRepository.flush();
                potentialErrorRepository.flush();
                articleRepository.clear();
            }
        }
    }

}
