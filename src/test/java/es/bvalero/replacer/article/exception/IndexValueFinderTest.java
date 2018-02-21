package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class IndexValueFinderTest {

    @Test
    public void testRegexIndexValue() {
        String text = "xxx | índice = yyyy \n zzz|param=value|title  = Hola\n Adiós }} ttt";

        IndexValueMatchFinder indexValueFinder = new IndexValueMatchFinder();
        List<RegexMatch> matches = indexValueFinder.findExceptionMatches(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertTrue(matches.contains(new RegexMatch(4, "| índice = yyyy \n zzz")));
    }

}
