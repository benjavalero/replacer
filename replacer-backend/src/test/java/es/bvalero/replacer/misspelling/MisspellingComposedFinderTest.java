package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.finder.ArticleReplacement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.util.*;

public class MisspellingComposedFinderTest {

    private MisspellingComposedFinder misspellingComposedFinder;

    @Before
    public void setUp() {
        misspellingComposedFinder = new MisspellingComposedFinder();
    }

    @Test
    public void testFindComposedMisspelling() {
        String text = "Y aún así vino.";
        Misspelling simple = Misspelling.ofCaseInsensitive("aún", "aun");
        Misspelling composed = Misspelling.ofCaseInsensitive("aún así", "aun así");
        Set<Misspelling> misspellings = new HashSet<>(Arrays.asList(simple, composed));

        // Fake the update of the misspelling list in the misspelling manager
        misspellingComposedFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, misspellings));

        List<ArticleReplacement> results = misspellingComposedFinder.findReplacements(text);

        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());

        ArticleReplacement result1 = results.get(0);
        Assert.assertEquals("aún así", result1.getText());
        Assert.assertEquals("aún así", result1.getSubtype());
        Assert.assertEquals("aun así", result1.getSuggestions().get(0).getText());
    }

}
