package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.finder.ArticleReplacement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class UppercaseAfterTest {

    @Mock
    private MisspellingManager misspellingManager;

    @InjectMocks
    private UppercaseAfterFinder uppercaseAfterFinder;

    @Before
    public void setUp() {
        uppercaseAfterFinder = new UppercaseAfterFinder();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRegexUppercaseAfter() {
        String noun1 = "Enero";
        String noun2 = "Febrero";
        String text = "{{ param=" + noun1 + " | " + noun2 + " }} zzz";

        Misspelling misspelling1 = Misspelling.builder().setWord("Enero").setCaseSensitive(true).setComment("enero").build();
        Misspelling misspelling2 = Misspelling.builder().setWord("Febrero").setCaseSensitive(true).setComment("febrero").build();
        uppercaseAfterFinder.buildMisspellingRelatedFields(new HashSet<>(Arrays.asList(misspelling1, misspelling2)));

        List<ArticleReplacement> matches = uppercaseAfterFinder.findIgnoredReplacements(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(noun1, matches.get(0).getText());
        Assert.assertEquals(9, matches.get(0).getStart());
        Assert.assertEquals(noun2, matches.get(1).getText());
        Assert.assertEquals(17, matches.get(1).getStart());
    }

}
