package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class FalsePositiveFinder implements ExceptionMatchFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(FalsePositiveFinder.class);
    private Pattern regexFalsePositives;

    private Pattern getRegexFalsePositives() {
        if (regexFalsePositives == null) {
            List<String> falsePositivesList = loadFalsePositives();
            String alternations = StringUtils.collectionToDelimitedString(falsePositivesList, "|");
            regexFalsePositives = Pattern.compile("\\b(?:" + alternations + ")\\b");
        }
        return regexFalsePositives;
    }

    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatches(text, getRegexFalsePositives());
    }

    List<String> loadFalsePositives() {
        List<String> falsePositivesList = new ArrayList<>();
        String falsePositivesPath = RegExUtils.class.getResource("/false-positives.txt").getFile();

        try (InputStream stream = new FileInputStream(falsePositivesPath);
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

}
