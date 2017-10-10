package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.utils.RegexMatch;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class MisspellingFinderTest {

    @Test
    public void testFindWords() {
        String text = "#hola-adiós. <!-- Comentario --> [[España, Francia]]. Hola, adiós.";

        MisspellingFinder misspellingFinder = new MisspellingFinder();
        List<RegexMatch> words = misspellingFinder.findTextWords(text);

        Assert.assertEquals(7, words.size());
        Assert.assertTrue(words.contains(new RegexMatch(1, "hola")));
        Assert.assertTrue(words.contains(new RegexMatch(6, "adiós")));
        Assert.assertTrue(words.contains(new RegexMatch(60, "adiós")));
        Assert.assertTrue(words.contains(new RegexMatch(18, "Comentario")));
        Assert.assertTrue(words.contains(new RegexMatch(35, "España")));
        Assert.assertTrue(words.contains(new RegexMatch(43, "Francia")));
        Assert.assertTrue(words.contains(new RegexMatch(54, "Hola")));
    }

    @Test
    public void testRegexWord() {
        String text = ":Españísima|";

        MisspellingFinder misspellingFinder = new MisspellingFinder();
        List<RegexMatch> words = misspellingFinder.findTextWords(text);

        Assert.assertEquals(1, words.size());
        Assert.assertTrue(words.contains(new RegexMatch(1, "Españísima")));
    }

    @Test
    public void testFindProposedFixes() {
        MisspellingFinder misspellingFinder = new MisspellingFinder();

        Misspelling misspelling1 = new Misspelling("españa", true,
                "Nombre propio", Collections.singletonList("España"));
        Assert.assertTrue(misspellingFinder.findProposedFixes("españa", misspelling1).contains("España"));

        Misspelling misspelling2 = new Misspelling("adios", false,
                "Falta la tilde", Collections.singletonList("adiós"));
        Assert.assertTrue(misspellingFinder.findProposedFixes("Adios", misspelling2).contains("Adiós"));
    }

}
