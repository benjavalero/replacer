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

@Component
public class FalsePositiveFinder implements ExceptionMatchFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(FalsePositiveFinder.class);
    private String regexFalsePositives;

    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatches(text, getRegexFalsePositives());
    }

    private String getRegexFalsePositives() {
        if (this.regexFalsePositives == null) {
            List<String> falsePositivesList = loadFalsePositives();
            this.regexFalsePositives = StringUtils.collectionToDelimitedString(falsePositivesList, "|");
        }
        return this.regexFalsePositives;
    }

    List<String> loadFalsePositives() {
        List<String> falsePositivesList = new ArrayList<>();
        String falsePositivesPath = RegExUtils.class.getResource("/false-positives.txt").getFile();

        try (InputStream stream = new FileInputStream(falsePositivesPath);
             BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            // Read File Line By Line
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (!org.springframework.util.StringUtils.isEmpty(strLine)) {
                    falsePositivesList.add(strLine);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error loading the list of false positives", e);
        }

        return falsePositivesList;
    }

}
