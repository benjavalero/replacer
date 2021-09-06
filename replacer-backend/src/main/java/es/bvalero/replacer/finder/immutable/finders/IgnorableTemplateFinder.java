package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/** Find some ignorable templates. In case they are found the complete text must be ignored. */
@Component
class IgnorableTemplateFinder extends ImmutableCheckedFinder {

    @Resource
    private List<String> ignorableTemplates;

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.MAX;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        String lowerContent = page.getContent().toLowerCase();
        for (String template : ignorableTemplates) {
            int start = lowerContent.indexOf(template);
            // Check we are not capturing the start of a non-ignorable template
            if (start >= 0 && FinderUtils.isWordCompleteInText(start, template, lowerContent)) {
                return List.of(buildCompleteMatchResult(page));
            }
        }
        // If we get here no ignorable template has been found
        return Collections.emptyList();
    }

    private MatchResult buildCompleteMatchResult(FinderPage page) {
        return LinearMatchResult.of(0, page.getContent());
    }
}
