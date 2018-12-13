package es.bvalero.replacer.article.exception;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.misspelling.MisspellingManager;
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

        RunAutomaton automaton = Mockito.mock(RunAutomaton.class);
        Mockito.when(automaton.newMatcher(Mockito.anyString())).thenReturn(Mockito.mock(AutomatonMatcher.class));
        Mockito.when(misspellingManager.getUppercaseAfterAutomaton()).thenReturn(automaton);
        Mockito.when(misspellingManager.getUppercaseLinkAutomaton()).thenReturn(automaton);

        List<ArticleReplacement> matches = properNounFinder.findIgnoredReplacements(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(noun, matches.get(0).getText());
    }

    @Test
    public void testRegexLowercaseNoun() {
        String noun1 = "Enero";
        String noun2 = "Febrero";
        String text = "{{ param=" + noun1 + " | " + noun2 + " }} zzz";

        @org.intellij.lang.annotations.RegExp
        String regexUppercase = "(Enero|Febrero)";
        RunAutomaton uppercaseAutomaton
                = new RunAutomaton(new RegExp(regexUppercase).toAutomaton(new DatatypesAutomatonProvider()));
        Mockito.when(misspellingManager.getUppercaseAfterAutomaton()).thenReturn(uppercaseAutomaton);

        RunAutomaton automaton = Mockito.mock(RunAutomaton.class);
        Mockito.when(automaton.newMatcher(Mockito.anyString())).thenReturn(Mockito.mock(AutomatonMatcher.class));
        Mockito.when(misspellingManager.getUppercaseLinkAutomaton()).thenReturn(automaton);

        List<ArticleReplacement> matches = properNounFinder.findIgnoredReplacements(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(noun1, matches.get(0).getText());
        Assert.assertEquals(9, matches.get(0).getStart());
        Assert.assertEquals(noun2, matches.get(1).getText());
        Assert.assertEquals(17, matches.get(1).getStart());
    }

}
