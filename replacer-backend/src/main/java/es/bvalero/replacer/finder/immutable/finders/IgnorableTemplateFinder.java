package es.bvalero.replacer.finder.immutable.finders;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/** Find some ignorable templates. In case they are found the complete text must be ignored. */
@Component
class IgnorableTemplateFinder extends ImmutableCheckedFinder {

    private RunAutomaton automaton;

    @Resource
    private List<String> ignorableTemplates;

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.MAX;
    }

    @PostConstruct
    public void init() {
        Set<String> fixedTemplates = ignorableTemplates
            .stream()
            .map(s -> s.replace("{", "\\{"))
            .map(s -> s.replace("#", "\\#"))
            .map(FinderUtils::toLowerCase)
            .collect(Collectors.toSet());
        String alternations = '(' + StringUtils.join(fixedTemplates, "|") + ')';
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(FinderUtils.toLowerCase(page.getContent()), automaton);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(
            match.start(),
            match.group(),
            FinderUtils.toLowerCase(page.getContent())
        );
    }

    @Override
    public Immutable convert(MatchResult match, FinderPage page) {
        return Immutable.of(0, page.getContent());
    }
}
