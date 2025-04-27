package es.bvalero.replacer.finder.immutable.finders;

import static org.apache.commons.lang3.StringUtils.SPACE;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find person surnames. Also, usual nouns preceded by a word starting in uppercase,
 * e.g. in Spanish `RCA Records`, as "records" is also a noun to be written with an accent.
 */
@Component
class PersonSurnameFinder implements ImmutableFinder {

    // Dependency injection
    private final FinderProperties finderProperties;

    private RunAutomaton automaton;
    private final Set<String> completeSurnames = new HashSet<>();

    PersonSurnameFinder(FinderProperties finderProperties) {
        this.finderProperties = finderProperties;
    }

    @PostConstruct
    public void init() {
        final Set<String> surnames = new HashSet<>();
        for (FinderProperties.PersonSurname personSurname : this.finderProperties.getPersonSurnames()) {
            final String surname = personSurname.getSurname();
            surnames.add(surname);
            if (personSurname.isIgnoreName()) {
                this.completeSurnames.add(surname);
            }
        }

        final String alternations = "<Lu><L>+ (" + FinderUtils.joinAlternate(surnames) + ")";
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public FinderPriority getPriority() {
        // It should be High for number of matches, but it is quite slow, so it is better to have lower priority.
        return FinderPriority.LOW;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // The list will keep on growing
        // The best approach is to find the whole list of words with the Aho-Corasick algorithm (2x faster),
        // but we choose the automaton because it allows regular expressions and the performance is quite good too.
        return AutomatonMatchFinder.find(page.getContent(), this.automaton);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }

    @Override
    public Immutable convert(MatchResult match, FinderPage page) {
        final int startSurname = match.group().indexOf(SPACE) + 1;
        final String surname = match.group().substring(startSurname);
        if (this.completeSurnames.contains(surname)) {
            return ImmutableFinder.super.convert(match, page);
        } else {
            return Immutable.of(match.start() + startSurname, surname);
        }
    }
}
