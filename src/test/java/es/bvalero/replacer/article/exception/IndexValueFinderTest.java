package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class IndexValueFinderTest {

    @Test
    public void testRegexIndexValue() {
        String value1 = "| índice = yyyy \\n zzz";
        String value2 = "| índice= xxx";
        String text = "{{Plantilla " + value1 + value2 + "}}";

        IndexValueFinder indexValueFinder = new IndexValueFinder();

        List<RegexMatch> matches = indexValueFinder.findExceptionMatches(text, false);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(value1, matches.get(0).getOriginalText());
        Assert.assertEquals(value2, matches.get(1).getOriginalText());

        matches = indexValueFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(value1, matches.get(0).getOriginalText());
        Assert.assertEquals(value2, matches.get(1).getOriginalText());
    }

}
