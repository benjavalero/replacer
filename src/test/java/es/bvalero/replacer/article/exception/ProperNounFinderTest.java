package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.misspelling.Misspelling;
import es.bvalero.replacer.misspelling.MisspellingManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ProperNounFinderTest {

    @Mock
    private MisspellingManager misspellingManager;

    @InjectMocks
    private ProperNounFinder properNounFinder;

    @Before
    public void setUp() {
        properNounFinder = new ProperNounFinder();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRegexProperNoun() {
        String noun = "Julio";
        String surname = "Verne";
        String text = "xxx " + noun + ' ' + surname + " zzz";

        List<ArticleReplacement> matches = properNounFinder.findIgnoredReplacements(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(1, matches.size());
        Assert.assertEquals(noun, matches.get(0).getText());
    }

    @Test
    public void testRegexLowercaseNoun() {
        String noun1 = "Enero";
        String noun2 = "Febrero";
        String text = "{{ param=" + noun1 + " | " + noun2 + " }} zzz";

        Misspelling misspelling1 = Misspelling.builder().setWord("Enero").setCaseSensitive(true).build();
        Misspelling misspelling2 = Misspelling.builder().setWord("Febrero").setCaseSensitive(true).build();
        Mockito.when(misspellingManager.getMisspellings())
                .thenReturn(new HashSet<>(Arrays.asList(misspelling1, misspelling2)));

        List<ArticleReplacement> matches = properNounFinder.findIgnoredReplacements(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(noun1, matches.get(0).getText());
        Assert.assertEquals(9, matches.get(0).getStart());
        Assert.assertEquals(noun2, matches.get(1).getText());
        Assert.assertEquals(17, matches.get(1).getStart());
    }

    @Test
    public void testRegexUppercaseLink() {
        String noun1 = "Enero";
        String noun2 = "Febrero";
        String text = "En [[" + noun1 + "|" + noun2 + "]].";

        Misspelling misspelling1 = Misspelling.builder().setWord("Enero").setCaseSensitive(true).build();
        Misspelling misspelling2 = Misspelling.builder().setWord("Febrero").setCaseSensitive(true).build();
        Mockito.when(misspellingManager.getMisspellings())
                .thenReturn(new HashSet<>(Arrays.asList(misspelling1, misspelling2)));

        List<ArticleReplacement> matches = properNounFinder.findIgnoredReplacements(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(noun1, matches.get(1).getText());
        // The noun2 is captured because of the "uppercase after"
        Assert.assertEquals(noun2, matches.get(0).getText());
    }

}
