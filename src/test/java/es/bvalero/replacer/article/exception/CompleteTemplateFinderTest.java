package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CompleteTemplateFinderTest {

    @Test
    public void testRegexCompleteTemplate() {
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
        Assert.assertEquals(StringUtils.escapeText(template1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(template2), matches.get(1).getOriginalText());
    }

    @Test
    public void testRegexCategory() {
        String category1 = "[[Categoría:Lluvia]]";
        String category2 = "[[Categoría:España cañí]]";
        String text = "xxx " + category1 + " / " + category2 + " zzz";

        CompleteTemplateFinder completeTemplateFinder = new CompleteTemplateFinder();

        List<RegexMatch> matches = completeTemplateFinder.findExceptionMatches(text, false);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(category1, matches.get(0).getOriginalText());
        Assert.assertEquals(category2, matches.get(1).getOriginalText());

        matches = completeTemplateFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(StringUtils.escapeText(category1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(category2), matches.get(1).getOriginalText());
    }

}
