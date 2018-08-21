package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ProperNounFinderTest {

    @Test
    public void testRegexProperNoun() {
        String noun = "Julio";
        String surname = "Verne";
        String text = "xxx " + noun + " " + surname + " zzz";

        ProperNounFinder properNounFinder = new ProperNounFinder();

        List<RegexMatch> matches = properNounFinder.findExceptionMatches(text, false);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(noun, matches.get(0).getOriginalText());

        matches = properNounFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(StringUtils.escapeText(noun), matches.get(0).getOriginalText());
    }

}
