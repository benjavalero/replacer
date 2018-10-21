package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleReplacementFinder;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class FalsePositiveFinder implements IgnoredReplacementFinder {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(FalsePositiveFinder.class);

    @Value("classpath:false-positives.txt")
    private Resource resource;
    private RunAutomaton falsePositivesAutomaton;

    @TestOnly
    void setResource(Resource resource) {
        this.resource = resource;
    }

    private synchronized RunAutomaton getFalsePositivesAutomaton() {
        if (falsePositivesAutomaton == null) { // For the first time
            List<String> falsePositivesList = loadFalsePositives();
            String alternations = StringUtils.collectionToDelimitedString(falsePositivesList, "|");
            RegExp r = new RegExp(alternations);
            falsePositivesAutomaton = new RunAutomaton(r.toAutomaton());
        }
        return falsePositivesAutomaton;
    }

    List<String> loadFalsePositives() {
        List<String> falsePositivesList = new ArrayList<>(100);

        try (Stream<String> stream = Files.lines(Paths.get(resource.getURI()), StandardCharsets.UTF_8)) {
            stream.forEach(strLine -> {
                String trim = strLine.trim();
                // Skip empty and commented lines
                if (!StringUtils.isEmpty(trim) && !trim.startsWith("#")) {
                    falsePositivesList.add(trim);
                }
            });
        } catch (IOException e) {
            LOGGER.error("Error loading the list of false positives", e);
        }

        return falsePositivesList;
    }

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        return ArticleReplacementFinder.findReplacements(text, getFalsePositivesAutomaton(), ReplacementType.IGNORED);
    }

}
