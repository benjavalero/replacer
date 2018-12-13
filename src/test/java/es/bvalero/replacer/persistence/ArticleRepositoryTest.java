package es.bvalero.replacer.persistence;

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
import java.util.Collection;
import java.util.Optional;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Test
    public void testInsert() {
        Assert.assertEquals(0L, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);

        Assert.assertEquals(1L, articleRepository.count());
        Optional<Article> dbArticle = articleRepository.findById(1);
        Assert.assertTrue(dbArticle.isPresent());
        dbArticle.ifPresent(article -> Assert.assertEquals(newArticle, article));

        // By default Addition Date is the current one
        dbArticle.ifPresent(article -> Assert.assertNotNull(article.getAdditionDate()));
    }

    @Test
    public void testInsertDuplicated() {
        Assert.assertEquals(0L, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);

        String title = "España";
        Article duplicated = new Article.ArticleBuilder().setId(1).setTitle(title).build();
        articleRepository.save(duplicated);

        // The second insert updates the first
        Assert.assertEquals(1L, articleRepository.count());
        Optional<Article> dbArticle = articleRepository.findById(1);
        Assert.assertTrue(dbArticle.isPresent());
        dbArticle.ifPresent(article -> Assert.assertEquals(title, article.getTitle()));
    }

    @Test
    public void testInsertWithReplacements() {
        Assert.assertEquals(0L, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticleId(newArticle.getId())
                .setType(ReplacementType.MISSPELLING)
                .setText("A").build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticleId(newArticle.getId())
                .setType(ReplacementType.MISSPELLING)
                .setText("B")
                .build();
        replacementRepository.saveAll(Arrays.asList(replacement1, replacement2));

        Assert.assertEquals(1L, articleRepository.count());
        Assert.assertEquals(2L, replacementRepository.count());
        Assert.assertEquals(2, replacementRepository.findByArticleId(newArticle.getId()).size());
    }

    @Test
    public void testModifyArticle() {
        Assert.assertEquals(0L, articleRepository.count());

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

        Assert.assertEquals(1L, articleRepository.count());
        articleRepository.findById(1).ifPresent(article -> {
            Assert.assertEquals(newTitle, article.getTitle());
            Assert.assertEquals(newAdditionDate, article.getAdditionDate());
            Assert.assertEquals(newReviewDate, article.getReviewDate());
        });
    }

    @Test
    public void testModifyReplacementList() {
        Assert.assertEquals(0L, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticleId(newArticle.getId())
                .setType(ReplacementType.MISSPELLING)
                .setText("A")
                .build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticleId(newArticle.getId())
                .setType(ReplacementType.MISSPELLING)
                .setText("B")
                .build();
        Replacement replacement3 = new Replacement.ReplacementBuilder()
                .setArticleId(newArticle.getId())
                .setType(ReplacementType.MISSPELLING)
                .setText("C")
                .build();
        replacementRepository.saveAll(Arrays.asList(replacement1, replacement2, replacement3));

        Assert.assertEquals(1L, articleRepository.count());
        Assert.assertEquals(3L, replacementRepository.count());
        Assert.assertEquals(3, replacementRepository.findByArticleId(newArticle.getId()).size());

        // Delete replacements
        replacementRepository.deleteInBatch(Arrays.asList(replacement1, replacement3));

        Assert.assertEquals(1L, articleRepository.count());
        Assert.assertEquals(1L, replacementRepository.count());
        Assert.assertEquals(1, replacementRepository.findByArticleId(newArticle.getId()).size());
        Assert.assertEquals("B", replacementRepository.findByArticleId(newArticle.getId()).get(0).getText());

        // Add replacements
        Replacement replacement4 = new Replacement.ReplacementBuilder()
                .setArticleId(newArticle.getId())
                .setType(ReplacementType.MISSPELLING)
                .setText("D")
                .build();
        replacementRepository.save(replacement4);

        Assert.assertEquals(1L, articleRepository.count());
        Assert.assertEquals(2L, replacementRepository.count());
        Assert.assertEquals(2, replacementRepository.findByArticleId(newArticle.getId()).size());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testInsertDuplicatedReplacement() {
        Assert.assertEquals(0L, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticleId(newArticle.getId())
                .setType(ReplacementType.MISSPELLING)
                .setText("A")
                .build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticleId(newArticle.getId())
                .setType(ReplacementType.MISSPELLING)
                .setText("A")
                .build();
        replacementRepository.saveAll(Arrays.asList(replacement1, replacement2));
    }

    @Test // (expected = DataIntegrityViolationException.class)
    public void testDeleteArticleWithReplacements() {
        Assert.assertEquals(0L, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticleId(newArticle.getId())
                .setType(ReplacementType.MISSPELLING)
                .setText("A")
                .build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticleId(newArticle.getId())
                .setType(ReplacementType.MISSPELLING)
                .setText("B")
                .build();
        replacementRepository.saveAll(Arrays.asList(replacement1, replacement2));

        Assert.assertEquals(1L, articleRepository.count());
        Assert.assertEquals(2L, replacementRepository.count());

        // This will NOT fail
        // We have removed the relation between the entities
        articleRepository.delete(newArticle);
        Assert.assertEquals(0L, articleRepository.count());
    }

    @Test
    public void testDeleteArticleInCascade() {
        Assert.assertEquals(0L, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticleId(newArticle.getId())
                .setType(ReplacementType.MISSPELLING)
                .setText("A")
                .build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticleId(newArticle.getId())
                .setType(ReplacementType.MISSPELLING)
                .setText("B")
                .build();
        replacementRepository.saveAll(Arrays.asList(replacement1, replacement2));

        Assert.assertEquals(1L, articleRepository.count());
        Assert.assertEquals(2L, replacementRepository.count());

        replacementRepository.deleteByArticleId(newArticle.getId());
        articleRepository.delete(newArticle);
        Assert.assertEquals(0L, articleRepository.count());
        Assert.assertEquals(0L, replacementRepository.count());
    }

    @Test
    @Ignore
    public void testPerformance() {
        Collection<Article> articles = new ArrayList<>(50);
        Collection<Replacement> replacements = new ArrayList<>(500);
        for (int i = 0; i < 1000000; i++) {
            Article newArticle = Article.builder().setId(i).setTitle("Title " + i).build();
            for (int j = 0; j < 10; j++) {
                replacements.add(Replacement.builder()
                        .setArticleId(newArticle.getId())
                        .setType(ReplacementType.MISSPELLING)
                        .setText("Text " + j)
                        .build());
            }
            articles.add(newArticle);
            if (articles.size() == 50) {
                articleRepository.saveAll(articles);
                articles.clear();
                replacementRepository.saveAll(replacements);
                replacements.clear();

                articleRepository.flush();
                replacementRepository.flush();
                articleRepository.clear();
            }
        }
    }

}
