package es.bvalero.replacer.article.exception;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleReplacementFinder;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import es.bvalero.replacer.misspelling.Misspelling;
import es.bvalero.replacer.misspelling.MisspellingManager;
import es.bvalero.replacer.persistence.ReplacementType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

@Component
public class ProperNounFinder implements IgnoredReplacementFinder, PropertyChangeListener {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(ProperNounFinder.class);

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_PROPER_NOUN = "(Domingo|Frances|Julio|Sidney)<Z><Lu>";
    private static final RunAutomaton AUTOMATON_PROPER_NOUN =
            new RunAutomaton(new RegExp(REGEX_PROPER_NOUN).toAutomaton(new DatatypesAutomatonProvider()));

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_UPPERCASE = "\\p{Lu}";
    private static final Pattern PATTERN_UPPERCASE = Pattern.compile(REGEX_UPPERCASE);

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_UPPERCASE_AFTER_PUNCTUATION = "[\\\\.!*#|=]<Z>?(%s)";

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_UPPERCASE_IN_LINK = "\\[\\[(%s)\\|";

    @Autowired
    private MisspellingManager misspellingManager;

    // Regex with the misspellings which start with uppercase and are case-sensitive
    // and starting with a special character which justifies the uppercase
    private RunAutomaton uppercaseAfterAutomaton;

    // Regex with the misspellings which start with uppercase in links, e. g. [[Hispano|hispanidad]]
    private RunAutomaton uppercaseLinkAutomaton;

    @PostConstruct
    public void init() {
        misspellingManager.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        @SuppressWarnings("unchecked")
        Set<Misspelling> newMisspellings = (Set<Misspelling>) evt.getNewValue();
        buildMisspellingRelatedFields(newMisspellings);
    }

    void buildMisspellingRelatedFields(Set<Misspelling> newMisspellings) {
        this.uppercaseAfterAutomaton = buildUppercaseAfterAutomaton(newMisspellings);
        this.uppercaseLinkAutomaton = buildUppercaseLinkAutomaton(newMisspellings);
    }

    private RunAutomaton buildUppercaseAfterAutomaton(Set<Misspelling> misspellings) {
        LOGGER.info("Start building uppercaseAfter automaton...");

        // Build an automaton with the misspellings starting with uppercase
        List<String> alternations = new ArrayList<>(misspellings.size());
        for (Misspelling misspelling : misspellings) {
            if (misspelling.isCaseSensitive() && MisspellingManager.startsWithUpperCase(misspelling.getWord())) {
                alternations.add(misspelling.getWord());
            }
        }
        String regexAlternations = String.format(REGEX_UPPERCASE_AFTER_PUNCTUATION, StringUtils.join(alternations, "|"));
        RunAutomaton automaton = new RunAutomaton(new RegExp(regexAlternations).toAutomaton(new DatatypesAutomatonProvider()));

        LOGGER.info("End building uppercaseAfter automaton");
        return automaton;
    }

    private RunAutomaton buildUppercaseLinkAutomaton(Set<Misspelling> misspellings) {
        LOGGER.info("Start building uppercaseLink automaton...");

        // Build an automaton with the misspellings starting with uppercase
        List<String> alternations = new ArrayList<>(misspellings.size());
        for (Misspelling misspelling : misspellings) {
            if (misspelling.isCaseSensitive() && MisspellingManager.startsWithUpperCase(misspelling.getWord())) {
                alternations.add(misspelling.getWord());
            }
        }
        String regexAlternations = String.format(REGEX_UPPERCASE_IN_LINK, StringUtils.join(alternations, "|"));
        RunAutomaton automaton = new RunAutomaton(new RegExp(regexAlternations).toAutomaton(new DatatypesAutomatonProvider()));

        LOGGER.info("End building uppercaseLink automaton");
        return automaton;
    }

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        List<ArticleReplacement> matches = new ArrayList<>(100);

        // Person names. We don't need the extra letter captured for the surname.
        for (ArticleReplacement match : ArticleReplacementFinder.findReplacements(text, AUTOMATON_PROPER_NOUN, ReplacementType.IGNORED)) {
            matches.add(match.withText(match.getText().substring(0, match.getText().length() - 2)));
        }

        // Lowercase nouns that start with uppercase because after some special character
        // We don't need the extra letters captured for the separator
        for (ArticleReplacement match : ArticleReplacementFinder.findReplacements(text, this.uppercaseAfterAutomaton, ReplacementType.IGNORED)) {
            // Find the letter position
            Matcher m = PATTERN_UPPERCASE.matcher(match.getText());
            if (m.find()) {
                matches.add(match
                        .withStart(match.getStart() + m.start())
                        .withText(match.getText().substring(m.start())));
            }
        }

        // Lowercase nouns that start with uppercase because in the first part of links
        // We don't need the extra letters captured for the separator
        for (ArticleReplacement match : ArticleReplacementFinder.findReplacements(text, this.uppercaseLinkAutomaton, ReplacementType.IGNORED)) {
            matches.add(match
                    .withStart(match.getStart() + 2)
                    .withText(match.getText().substring(2, match.getText().length() - 1)));
        }

        return matches;
    }

}
