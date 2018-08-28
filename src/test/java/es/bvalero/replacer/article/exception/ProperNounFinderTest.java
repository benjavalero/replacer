package es.bvalero.replacer.article.exception;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.misspelling.MisspellingManager;
import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class ProperNounFinderTest {

    @Mock
    MisspellingManager misspellingManager;

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
        String text = "xxx " + noun + " " + surname + " zzz";

        List<RegexMatch> matches = properNounFinder.findExceptionMatches(text, false);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(noun, matches.get(0).getOriginalText());

        matches = properNounFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(StringUtils.escapeText(noun), matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexLowercaseNoun() {
        String noun1 = "Enero";
        String noun2 = "Febrero";
        String text = "{{ param=" + noun1 + " | " + noun2 + " }} zzz";

        String regexUppercaseAlternations = "[\\.!\\*#\\|=]<Z>?(Enero|Febrero)";
        RunAutomaton uppercaseMisspellingsAutomaton
                = new RunAutomaton(new RegExp(regexUppercaseAlternations).toAutomaton(new DatatypesAutomatonProvider()));
        Mockito.when(misspellingManager.getUppercaseMisspellingsAutomaton()).thenReturn(uppercaseMisspellingsAutomaton);

        List<RegexMatch> matches = properNounFinder.findExceptionMatches(text, false);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(noun1, matches.get(0).getOriginalText());
        Assert.assertEquals(noun2, matches.get(1).getOriginalText());

        matches = properNounFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(StringUtils.escapeText(noun1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(noun2), matches.get(1).getOriginalText());
    }

}
