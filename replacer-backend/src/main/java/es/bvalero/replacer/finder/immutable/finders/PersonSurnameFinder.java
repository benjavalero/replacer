package es.bvalero.replacer.finder.immutable.finders;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.Immutable;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.HashSet;
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

    private final Set<String> completeSurnames = new HashSet<>();

    @Override
    public FinderPriority getPriority() {
        // It should be High for number of matches but it is quite slow so it is better to have lower priority
        return FinderPriority.LOW;
    }

    @PostConstruct
    public void init() {
        final Set<String> surnames = new HashSet<>();
        for (String personSurname : personSurnames) {
            if (personSurname.startsWith("*")) {
                final String surname = personSurname.substring(1);
                surnames.add(surname);
                completeSurnames.add(surname);
            } else {
                surnames.add(personSurname);
            }
        }

        final String alternations = "<Lu><L>+ (" + StringUtils.join(surnames, "|") + ")";
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        // The list will keep on growing
        // The best approach is to iterate the list of words and find them in the text but we choose
        // the automaton because it allows regular expressions and the performance is quite good too
        return AutomatonMatchFinder.find(page.getContent(), automaton);
    }

    @Override
    public boolean validate(MatchResult match, WikipediaPage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }

    @Override
    public Immutable convert(MatchResult match, WikipediaPage page) {
        final int pos = match.group().indexOf(' ') + 1;
        final int start = match.start() + pos;
        final String matchText = match.group().substring(pos);
        if (completeSurnames.contains(matchText)) {
            return ImmutableFinder.super.convert(match, page);
        } else {
            return Immutable.of(start, matchText);
        }
    }
}
