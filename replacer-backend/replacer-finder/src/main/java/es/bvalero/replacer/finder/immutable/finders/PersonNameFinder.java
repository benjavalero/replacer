package es.bvalero.replacer.finder.immutable.finders;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import jakarta.annotation.PostConstruct;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * Find person names which are used also as nouns and thus are false positives,
 * e.g. in Spanish `Julio` in `Julio Verne`, as "julio" is also the name of a month
 * to be written in lowercase.
 * <br />
 * It also finds words used commonly in titles, as `Sky` in `Sky News`.
 * Or compound words, as `Los Angeles`.
 * <br />
 * The implementation uses a single alternate automaton built from all the person names.
 * This was the original approach before commit 8102f24cb, which switched to Aho-Corasick
 * (WholeWordLongestMatchMap). Benchmarks confirm the alternate automaton is ~20% faster.
 */
@Component
public class PersonNameFinder implements ImmutableFinder {

    // Dependency injection
    private final FinderProperties finderProperties;

    private RunAutomaton automaton;

    public PersonNameFinder(FinderProperties finderProperties) {
        this.finderProperties = finderProperties;
    }

    @PostConstruct
    public void init() {
        String alternations = String.format("(%s)", FinderUtils.joinAlternate(this.finderProperties.getPersonNames()));
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public FinderPriority getPriority() {
        // It should be High for number of matches, but it is slow, so it is better to have lower priority.
        return FinderPriority.MEDIUM;
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(page.getContent(), this.automaton);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordFollowedByUpperCase(match.start(), match.group(), page.getContent());
    }
}
