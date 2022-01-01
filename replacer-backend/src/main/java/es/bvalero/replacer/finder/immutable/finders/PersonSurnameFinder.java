package es.bvalero.replacer.finder.immutable.finders;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Set;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Find person surnames. Also usual nouns preceded by a word starting in uppercase,
 * e.g. in Spanish `RCA Records`, as "records" is also a noun to be written with an accent.
 */
@Component
class PersonSurnameFinder implements ImmutableFinder {

    private RunAutomaton automaton;

    @Resource
    private Set<String> personSurnames;

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.HIGH;
    }

    @PostConstruct
    public void init() {
        final String alternations = "<Lu><L>+ (" + StringUtils.join(personSurnames, "|") + ")";
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // The list will keep on growing
        // The best approach is to iterate the list of words and find them in the text but we choose
        // the automaton because it allows regular expressions and the performance is quite good too
        return AutomatonMatchFinder.find(page.getContent(), automaton);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }

    @Override
    public Immutable convert(MatchResult match, FinderPage page) {
        final int pos = match.group().indexOf(' ') + 1;
        final int start = match.start() + pos;
        final String matchText = match.group().substring(pos);
        return Immutable.of(start, matchText);
    }
}
