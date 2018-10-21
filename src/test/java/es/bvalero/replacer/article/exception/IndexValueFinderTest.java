package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class IndexValueFinderTest {

    @Test
    public void testRegexIndexValue() {
        String value1 = "yyyy \\n zzz";
        String value2 = "xxx";
        String text = "{{Plantilla | índice = " + value1 + "|index= " + value2 + "}}";

        IgnoredReplacementFinder indexValueFinder = new IndexValueFinder();

        List<ArticleReplacement> matches = indexValueFinder.findIgnoredReplacements(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(value1, matches.get(0).getText());
        Assert.assertEquals(value2, matches.get(1).getText());
    }

}
