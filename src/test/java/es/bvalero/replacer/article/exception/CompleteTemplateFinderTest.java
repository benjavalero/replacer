package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CompleteTemplateFinderTest {

    @Test
    public void testRegexUnreplacableTemplate() {
        String template1 = "{{Cita|xxx {{Fecha|zzz}} yyy}}";
        String template2 = "{{quote|bbb}}";
        String text = "xxx " + template1 + " / " + template2 + " zzz";

        CompleteTemplateFinder completeTemplateFinder = new CompleteTemplateFinder();

        List<RegexMatch> matches = completeTemplateFinder.findExceptionMatches(text, false);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(template1, matches.get(0).getOriginalText());
        Assert.assertEquals(template2, matches.get(1).getOriginalText());

        matches = completeTemplateFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(template1, matches.get(0).getOriginalText());
        Assert.assertEquals(template2, matches.get(1).getOriginalText());
    }

    @Test
    public void testRegexCategory() {
        String category = "[[Categoría:Lluvia]]";
        String text = "xxx " + category + " zzz";

        CompleteTemplateFinder completeTemplateFinder = new CompleteTemplateFinder();

        List<RegexMatch> matches = completeTemplateFinder.findExceptionMatches(text, false);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(category, matches.get(0).getOriginalText());

        matches = completeTemplateFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(category, matches.get(0).getOriginalText());
    }

}
