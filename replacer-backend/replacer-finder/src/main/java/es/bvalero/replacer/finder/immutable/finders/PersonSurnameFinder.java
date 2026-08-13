package es.bvalero.replacer.finder.immutable.finders;

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
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * Find person surnames. Also, usual nouns preceded by a word starting in uppercase,
 * e.g. in Spanish `RCA Records`, as "records" is also a noun to be written with an accent.
 * <br />
 * The implementation uses a single alternate automaton built from all the surnames.
 * The preceding uppercase word (the name) is validated in {@link #validate} via
 * {@link FinderUtils#isWordPrecededByUpperCase}, which is faster than embedding
 * the {@code <Lu><L>+} prefix in the automaton pattern (previous approach).
 * Benchmarks confirm the alternate automaton is ~12% faster than the complete approach.
 */
@Component
public class PersonSurnameFinder implements ImmutableFinder {

    // Dependency injection
    private final FinderProperties finderProperties;

    private RunAutomaton automaton;
    private final Set<String> completeSurnames = new HashSet<>();

    public PersonSurnameFinder(FinderProperties finderProperties) {
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

        final String alternations = String.format("(%s)", FinderUtils.joinAlternate(surnames));
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public FinderPriority getPriority() {
        // It should be High for number of matches, but it is quite slow, so it is better to have lower priority.
        return FinderPriority.LOW;
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(page.getContent(), this.automaton);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return (
            FinderUtils.isWordCompleteInText(match, page.getContent()) &&
            FinderUtils.isWordPrecededByUpperCase(match.start(), page.getContent())
        );
    }

    @Override
    public Immutable convert(MatchResult match, FinderPage page) {
        if (this.completeSurnames.contains(match.group())) {
            // Include the preceding name in the immutable
            final MatchResult wordBefore = FinderUtils.findWordBefore(page.getContent(), match.start());
            if (wordBefore != null) {
                return Immutable.of(wordBefore.start(), wordBefore.group() + " " + match.group());
            }
        }
        return ImmutableFinder.super.convert(match, page);
    }
}
