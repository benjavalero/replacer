package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class FalsePositiveFinder implements ExceptionMatchFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(FalsePositiveFinder.class);

    @Autowired
    private ResourceLoader resourceLoader;

    private RunAutomaton falsePositivesAutomaton = null;

    @NotNull
    private synchronized RunAutomaton getFalsePositivesAutomaton() {
        if (this.falsePositivesAutomaton == null) { // For the first time
            List<String> falsePositivesList = loadFalsePositives();
            String alternations = StringUtils.collectionToDelimitedString(falsePositivesList, "|");
            RegExp r = new RegExp(alternations);
            this.falsePositivesAutomaton = new RunAutomaton(r.toAutomaton());
        }
        return this.falsePositivesAutomaton;
    }

    List<String> loadFalsePositives() {
        List<String> falsePositivesList = new ArrayList<>(150);

        try (InputStream stream = resourceLoader.getResource("classpath:false-positives.txt").getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            // Read File Line By Line
            String strLine;
            while ((strLine = br.readLine()) != null) {
                strLine = strLine.trim();
                // Skip empty and commented lines
                if (!org.springframework.util.StringUtils.isEmpty(strLine) && !strLine.startsWith("#")) {
                    falsePositivesList.add(strLine);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error loading the list of false positives", e);
        }

        return falsePositivesList;
    }

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatchesAutomaton(text, getFalsePositivesAutomaton());
    }

}
