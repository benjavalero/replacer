package es.bvalero.replacer.finder.immutable.finders;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.Immutable;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/** Find some ignorable templates. In case they are found the complete text must be ignored. */
@Component
class IgnorableTemplateFinder implements ImmutableFinder {

    private RunAutomaton automaton;

    @Resource
    private List<String> ignorableTemplates;

    @Override
    public FinderPriority getPriority() {
        // It's slow, but we are interested in ignoring complete pages.
        return FinderPriority.MAX;
    }

    @PostConstruct
    public void init() {
        final List<String> fixedTemplates = ignorableTemplates
            .stream()
            .map(s -> s.replace("{", "\\{"))
            .map(s -> s.replace("#", "\\#"))
            .map(FinderUtils::toLowerCase)
            .collect(Collectors.toUnmodifiableList());
        final String alternations = String.format("(%s)", FinderUtils.joinAlternate(fixedTemplates));
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return AutomatonMatchFinder.find(FinderUtils.toLowerCase(page.getContent()), automaton);
    }

    @Override
    public boolean validate(MatchResult match, WikipediaPage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }

    @Override
    public Immutable convert(MatchResult match, WikipediaPage page) {
        return Immutable.of(0, page.getContent());
    }
}
