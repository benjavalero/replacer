package es.bvalero.replacer.finder.immutable.finders;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Set;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * Find person names which are used also as nouns and thus are false positives,
 * e.g. in Spanish `Julio` in `Julio Verne`, as "julio" is also the name of a month
 * to be written in lowercase.
 * <br />
 * It also finds words used commonly in titles, as `Sky` in `Sky News`.
 * Or compound words, as `Los Angeles`.
 */
@Component
class PersonNameFinder implements ImmutableFinder {

    private RunAutomaton automaton;

    @Resource
    private Set<String> personNames;

    @Override
    public FinderPriority getPriority() {
        // It should be High for number of matches, but it is slow, so it is better to have lower priority.
        return FinderPriority.MEDIUM;
    }

    @PostConstruct
    public void init() {
        final String alternations = "(" + FinderUtils.joinAlternate(this.personNames) + ")";
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // The list will keep on growing
        // There are now difference among the approaches,
        // so we decide to use an automaton for the sake of simplicity.
        return AutomatonMatchFinder.find(page.getContent(), this.automaton);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordFollowedByUpperCase(match.start(), match.group(), page.getContent());
    }
}
