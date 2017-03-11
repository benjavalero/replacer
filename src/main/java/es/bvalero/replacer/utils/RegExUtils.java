package es.bvalero.replacer.utils;

import es.bvalero.replacer.domain.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExUtils {

    /* \w doesn't include accentuated characters. \w includes the underscore. */
    static final String REGEX_WORD = "\\b[\\wÁáÉéÍíÓóÚúÜüÑñ]+\\b";
    // We allow the different exceptions in a text to overlap
    static final String REGEX_TEMPLATE_PARAM = "\\|[\\wÁáÉéÍíÓóÚúÜüÑñ\\s]+=";
    static final String REGEX_PARAM_VALUE = "\\|\\s*(?:índice|title)\\s*=[^}|]*";
    static final String REGEX_UNREPLACEBLE_TEMPLATE = "\\{\\{(?:ORDENAR:|DEFAULTSORT:|NF\\||[Cc]ita\\||c?[Qq]uote\\||coord\\|)[^}]+}}";
    static final String REGEX_TEMPLATE_NAME = "\\{\\{[^|}]+";
    // We trust the quotes are well formed with matching leading and trailing quotes
    static final String REGEX_QUOTES = "'{2,5}.+?'{2,5}";
    static final String REGEX_QUOTES_ESCAPED = "(&apos;){2,5}.+?(&apos;){2,5}";
    static final String REGEX_ANGULAR_QUOTES = "«[^»]+»";
    static final String REGEX_TYPOGRAPHIC_QUOTES = "“[^”]+”";
    static final String REGEX_DOUBLE_QUOTES = "\"[^\"]+\"";
    static final String REGEX_DOUBLE_QUOTES_ESCAPED = "&quot;.+?&quot;";
    static final String REGEX_FILE_NAME = "[=|:][^=|:]+\\.(?:svg|jpe?g|JPG|png|PNG|gif|ogg|pdf)";
    static final String REGEX_REF_NAME = "<ref\\s+name\\s+=[^>]+>";
    static final String REGEX_REF_NAME_ESCAPED = "&lt;ref\\s+name\\s+=.+?&gt;";
    static final String REGEX_CATEGORY = "\\[\\[Categoría:[^]]+]]";
    static final String REGEX_COMMENT = "<!--.*?-->";
    static final String REGEX_COMMENT_ESCAPED = "&lt;!--.*?--&gt;";
    static final String REGEX_URL = "https?://[\\w./\\-+?&%=:#;~]+";
    static final String REGEX_TAG_MATH = "<math>.*?</math>";
    static final String REGEX_TAG_SOURCE = "<source>.*?</source>";
    static final String REGEX_HEADERS = "={2,}.+?={2,}";
    static final String REGEX_WIKILINK = "\\[\\[[^\\]]+\\]\\]";
    private static final String TAG_REDIRECTION = "#REDIRECCIÓN";
    private static final String TAG_REDIRECT = "#REDIRECT";
    private static final Logger LOGGER = LoggerFactory.getLogger(RegExUtils.class);
    private static final List<Pattern> exceptionPatterns = new ArrayList<>();
    private static String regexFalsePositives = null;

    /**
     * Returns a map with all the words in the text and their position
     */
    public static Map<Integer, String> findWords(String text) {
        Map<Integer, String> words = new HashMap<>();
        Pattern pattern = Pattern.compile(REGEX_WORD);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            words.put(matcher.start(), matcher.group(0));
        }
        return words;
    }

    /**
     * Returns a list of intervals containing exceptions of the text
     */
    public static List<Interval> findExceptionIntervals(String text) {
        // TODO Try to merge the escaped regex with the original ones
        // TODO Try to merge the math and source tag regex
        if (exceptionPatterns.isEmpty()) {
            exceptionPatterns.add(Pattern.compile(REGEX_TEMPLATE_PARAM));
            exceptionPatterns.add(Pattern.compile(REGEX_PARAM_VALUE));
            exceptionPatterns.add(Pattern.compile(REGEX_UNREPLACEBLE_TEMPLATE));
            exceptionPatterns.add(Pattern.compile(REGEX_TEMPLATE_NAME));
            exceptionPatterns.add(Pattern.compile(REGEX_QUOTES));
            exceptionPatterns.add(Pattern.compile(REGEX_QUOTES_ESCAPED));
            exceptionPatterns.add(Pattern.compile(REGEX_ANGULAR_QUOTES));
            exceptionPatterns.add(Pattern.compile(REGEX_TYPOGRAPHIC_QUOTES));
            exceptionPatterns.add(Pattern.compile(REGEX_DOUBLE_QUOTES));
            exceptionPatterns.add(Pattern.compile(REGEX_DOUBLE_QUOTES_ESCAPED));
            exceptionPatterns.add(Pattern.compile(REGEX_FILE_NAME));
            exceptionPatterns.add(Pattern.compile(REGEX_REF_NAME));
            exceptionPatterns.add(Pattern.compile(REGEX_REF_NAME_ESCAPED));
            exceptionPatterns.add(Pattern.compile(REGEX_CATEGORY));
            exceptionPatterns.add(Pattern.compile(REGEX_COMMENT, Pattern.DOTALL));
            exceptionPatterns.add(Pattern.compile(REGEX_COMMENT_ESCAPED, Pattern.DOTALL));
            exceptionPatterns.add(Pattern.compile(REGEX_URL));
            exceptionPatterns.add(Pattern.compile(REGEX_TAG_MATH, Pattern.DOTALL));
            exceptionPatterns.add(Pattern.compile(REGEX_TAG_SOURCE, Pattern.DOTALL));
            exceptionPatterns.add(Pattern.compile(getRegexFalsePositives()));
        }

        List<Interval> exceptionIntervals = new ArrayList<>();
        for (Pattern pattern : exceptionPatterns) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                Interval exceptionInterval = new Interval(matcher.start(), matcher.end());
                exceptionIntervals.add(exceptionInterval);
            }
        }
        return exceptionIntervals;
    }

    static List<String> loadFalsePositives() {
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

    static String getRegexFalsePositives() {
        if (regexFalsePositives == null) {
            List<String> falsePositivesList = loadFalsePositives();
            regexFalsePositives = org.springframework.util.StringUtils.collectionToDelimitedString(falsePositivesList, "|");
        }
        return regexFalsePositives;
    }

    public static boolean isRedirectionArticle(String content) {
        return content.contains(TAG_REDIRECTION) || content.contains(TAG_REDIRECT);
    }

}
