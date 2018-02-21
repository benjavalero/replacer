package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UnreplaceableTemplateFinderTest {

    @Test
    public void testRegexUnreplacableTemplate() {
        String template = "{{Cita| yyyy \n zzz }}";
        String text = "xxx " + template + " zzz";

        UnreplaceableTemplateMatchFinder unreplaceableTemplateFinder = new UnreplaceableTemplateMatchFinder();
        List<RegexMatch> matches = unreplaceableTemplateFinder.findExceptionMatches(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(template, matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexCategory() {
        String category = "[[Categoría:Lluvia]]";
        String text = "xxx " + category + " zzz";

        UnreplaceableTemplateMatchFinder unreplaceableTemplateFinder = new UnreplaceableTemplateMatchFinder();
        List<RegexMatch> matches = unreplaceableTemplateFinder.findExceptionMatches(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(category, matches.get(0).getOriginalText());
    }

}
