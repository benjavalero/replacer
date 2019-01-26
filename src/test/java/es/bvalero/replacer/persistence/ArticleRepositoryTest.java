package es.bvalero.replacer.persistence;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
        Article dbArticle = articleRepository.findOne(1);
        Assert.assertNotNull(dbArticle);
        Assert.assertEquals(newArticle, dbArticle);

        // By default Addition Date is the current one
        Assert.assertNotNull(dbArticle.getLastUpdate());
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
        Article dbArticle = articleRepository.findOne(1);
        Assert.assertNotNull(dbArticle);
        Assert.assertEquals(title, dbArticle.getTitle());
    }

    @Test
    public void testInsertWithReplacements() {
        Assert.assertEquals(0L, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticle(newArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("A").build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticle(newArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("B")
                .build();
        replacementRepository.save(Arrays.asList(replacement1, replacement2));

        Assert.assertEquals(1L, articleRepository.count());
        Assert.assertEquals(2L, replacementRepository.count());
        Assert.assertEquals(2, replacementRepository.findByArticle(newArticle).size());
    }

    @Test
    public void testModifyArticle() {
        Assert.assertEquals(0L, articleRepository.count());

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").setLastUpdate(yesterday).build();
        articleRepository.save(newArticle);

        Assert.assertEquals(yesterday, articleRepository.findOne(1).getLastUpdate());

        // Modify attributes
        String newTitle = "España";
        Article toSave = newArticle
                .withTitle(newTitle)
                .withLastUpdate(today);
        articleRepository.save(toSave);

        Assert.assertEquals(1L, articleRepository.count());
        Assert.assertEquals(newTitle, articleRepository.findOne(1).getTitle());
        Assert.assertEquals(today, articleRepository.findOne(1).getLastUpdate());
    }

    @Test
    public void testModifyReplacementList() {
        Assert.assertEquals(0L, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticle(newArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("A")
                .build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticle(newArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("B")
                .build();
        Replacement replacement3 = new Replacement.ReplacementBuilder()
                .setArticle(newArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("C")
                .build();
        replacementRepository.save(Arrays.asList(replacement1, replacement2, replacement3));

        Assert.assertEquals(1L, articleRepository.count());
        Assert.assertEquals(3L, replacementRepository.count());
        Assert.assertEquals(3, replacementRepository.findByArticle(newArticle).size());

        // Delete replacements
        replacementRepository.deleteInBatch(Arrays.asList(replacement1, replacement3));

        Assert.assertEquals(1L, articleRepository.count());
        Assert.assertEquals(1L, replacementRepository.count());
        Assert.assertEquals(1, replacementRepository.findByArticle(newArticle).size());
        Assert.assertEquals("B", replacementRepository.findByArticle(newArticle).get(0).getText());

        // Add replacements
        Replacement replacement4 = new Replacement.ReplacementBuilder()
                .setArticle(newArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("D")
                .build();
        replacementRepository.save(replacement4);

        Assert.assertEquals(1L, articleRepository.count());
        Assert.assertEquals(2L, replacementRepository.count());
        Assert.assertEquals(2, replacementRepository.findByArticle(newArticle).size());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testInsertDuplicatedReplacement() {
        Assert.assertEquals(0L, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticle(newArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("A")
                .build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticle(newArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("A")
                .build();
        replacementRepository.save(Arrays.asList(replacement1, replacement2));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testDeleteArticleWithReplacements() {
        Assert.assertEquals(0L, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticle(newArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("A")
                .build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticle(newArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("B")
                .build();
        replacementRepository.save(Arrays.asList(replacement1, replacement2));

        Assert.assertEquals(1L, articleRepository.count());
        Assert.assertEquals(2L, replacementRepository.count());

        // This will fail
        // We have removed partially the relation between the entities but we keep the FK in Replacement
        articleRepository.delete(newArticle);
        Assert.assertEquals(0L, articleRepository.count());
    }

    @Test
    public void testDeleteArticleInCascade() {
        Assert.assertEquals(0L, articleRepository.count());

        Article newArticle = new Article.ArticleBuilder().setId(1).setTitle("Andorra").build();
        articleRepository.save(newArticle);
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticle(newArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("A")
                .build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticle(newArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("B")
                .build();
        replacementRepository.save(Arrays.asList(replacement1, replacement2));

        Assert.assertEquals(1L, articleRepository.count());
        Assert.assertEquals(2L, replacementRepository.count());

        replacementRepository.deleteByArticle(newArticle);
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
                        .setArticle(newArticle)
                        .setType(ReplacementType.MISSPELLING)
                        .setText("Text " + j)
                        .build());
            }
            articles.add(newArticle);
            if (articles.size() == 50) {
                articleRepository.save(articles);
                articles.clear();
                replacementRepository.save(replacements);
                replacements.clear();

                articleRepository.flush();
                replacementRepository.flush();
                articleRepository.clear();
            }
        }
    }

}
