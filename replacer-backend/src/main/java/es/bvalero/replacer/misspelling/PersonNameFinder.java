package es.bvalero.replacer.misspelling;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class PersonNameFinder implements IgnoredReplacementFinder {

    private RunAutomaton automaton;

    @PostConstruct
    public void init() {
        Collection<String> words = Arrays.asList("Domingo", "Frances", "Julio", "Sidney");
        String alternations = "(" + StringUtils.join(words, "|") + ").<Lu>";
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        List<ArticleReplacement> articleReplacements = new ArrayList<>(100);

        // Find all the words and check if they are potential errors
        AutomatonMatcher m = this.automaton.newMatcher(text);
        while (m.find()) {
            String word = m.group().substring(0, m.group().length() - 2);
            articleReplacements.add(ArticleReplacement.builder()
                    .setStart(m.start())
                    .setText(word)
                    .setType(ReplacementType.IGNORED)
                    .build());
        }

        return articleReplacements;
    }

}
