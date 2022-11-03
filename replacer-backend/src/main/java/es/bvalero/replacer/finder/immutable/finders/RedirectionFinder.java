package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/** Find redirection templates. In case they are found the complete text must be ignored. */
@Component
class RedirectionFinder implements ImmutableFinder {

    @Resource
    private Set<String> ignorableTemplates;

    private final Set<String> redirectionTemplates = new HashSet<>();

    @Override
    public FinderPriority getPriority() {
        // Redirections should be discarded using the information in the dump
        // This is run lastly just in case
        return FinderPriority.NONE;
    }

    @PostConstruct
    public void init() {
        this.redirectionTemplates.addAll(
                ignorableTemplates
                    .stream()
                    .filter(s -> s.contains("#"))
                    .map(FinderUtils::toLowerCase)
                    .collect(Collectors.toUnmodifiableSet())
            );
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        final String text = page.getContent();
        final String lowerCaseText = FinderUtils.toLowerCase(text);
        for (String redirectionTemplate : redirectionTemplates) {
            if (lowerCaseText.contains(redirectionTemplate)) {
                return Set.of(LinearMatchResult.of(0, text));
            }
        }
        return Collections.emptySet();
    }
}
