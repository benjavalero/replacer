package es.bvalero.replacer.article.exception;

import dk.brics.automaton.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegexPerformanceExperiments {
    private static final int NUM_RUNS = 100000000;
    private static final int NUM_WARM_UP_RUNS = 4;
    private static String mediumText = null;
    private static String longText = null;
    private static String longestText = null;

    static {
        try {
            mediumText = new String(Files.readAllBytes(Paths.get(RegexPerformanceExperiments.class.getResource("/article-medium.txt").toURI())),
                    StandardCharsets.UTF_8);
            longText = new String(Files.readAllBytes(Paths.get(RegexPerformanceExperiments.class.getResource("/article-long.txt").toURI())),
                    StandardCharsets.UTF_8);
            longestText = new String(Files.readAllBytes(Paths.get(RegexPerformanceExperiments.class.getResource("/article-longest.txt").toURI())),
                    StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // falsePositivesExperiment();
        // angularQuotesExperiment();
        // doubleQuotesExperiment();
        // singleQuotesExperiment();
        // templateNameExperiment();
        // xmlTagExperiment();
        xmlEntityExperiment();
        // completeTemplateExperiment();
        // indexValueExperiment();
        // wordExperiment();
        // fileNameExperiment();
        // urlExperiment();
        // completeTagExperiment();
        // templateParamExperiment();
    }

    private static void falsePositivesExperiment() {
        write("************** BEGIN FALSE POSITIVES EXPERIMENT *****************");

        // Load exceptions
        List<String> falsePositivesList = FalsePositiveFinder.loadFalsePositives();
        String alternationsRegex = StringUtils.collectionToDelimitedString(falsePositivesList, "|");
        String alternationsBracketRegex = StringUtils.collectionToDelimitedString(falsePositivesList, "|");
        String alternationsIgnoredRegex = "(?:" + alternationsRegex + ")";
        String alternationsBoundIgnoredRegex = "\\b" + alternationsIgnoredRegex + "\\b";
        String alternationsBoundRegex = "\\b(" + alternationsRegex + ")\\b";

        for (int i = 0; i < NUM_WARM_UP_RUNS; i++) {
            runExperiment(alternationsRegex, "ALTERNATIONS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(alternationsIgnoredRegex, "IGNORED REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(alternationsBracketRegex, "BRACKET REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(alternationsBoundIgnoredRegex, "BOUND IGNORED REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(alternationsBoundRegex, "BOUND REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperimentAutomaton(alternationsRegex, "ALTERNATIONS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(alternationsIgnoredRegex, "IGNORED REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(alternationsBracketRegex, "BRACKET REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(alternationsBoundIgnoredRegex, "BOUND IGNORED REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(alternationsBoundRegex, "BOUND REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
        }
        write("**************END EXPERIMENT*****************\n\n");
    }

    private static void angularQuotesExperiment() {
        System.out.println("BEGIN ANGULAR QUOTES EXPERIMENT...");

        // With the Automaton, the dot is really greedy, and it matches even the », so it fails with several matches.
        //String regex1 = "(?s)«.+»";
        String regex2 = "(?s)«.+?»";
        //String regex3 = "(?s)«.++»";
        String regex4 = "«[^»]+»";
        String regex5 = "«[^»]+?»";
        String regex6 = "«[^»]++»";

        for (int i = 1; i <= NUM_WARM_UP_RUNS; i++) {
            boolean isRealRun = (i == NUM_WARM_UP_RUNS);

            // The text-directed ones are really fast so we test only them
            // runExperiment(regex2, "2", isRealRun, false);
            // runExperiment(regex4, "4", isRealRun, false);
            runExperiment(regex4, "4", isRealRun, true);
            // runExperiment(regex5, "5", isRealRun, false);
            runExperiment(regex5, "5", isRealRun, true);
            // runExperiment(regex6, "6", isRealRun, false);
            runExperiment(regex6, "6", isRealRun, true);
        }

        // Medium-text : regex6 is 10% slower than regex5
        // Long-text : regex6 is 40% faster than regex5
        // Longest-text : regex6 is 50% faster than regex5
        // Thus we prefer a little penalty for medium-texts and choose the regex6
    }

    private static void doubleQuotesExperiment() {
        write("**************BEGIN DOUBLE QUOTES EXPERIMENT *****************");
        String lazyRegex = "\".+?\"";
        String greedyClassRegex = "\"[^\"\n]+\"";
        String lazyClassRegex = "\"[^\"\n]+?\"";
        String possessiveClassRegex = "\"[^\"]++\"";
        String limitedPossessiveClassRegex = "\\b\"[^\"]++\"\\b";
        String backReferenceRegex = "(?s)(\"|&quot;).+?\\1"; // It takes a little more but it is actually two (or more) regex in one
        String greedyClassRegexAutomaton = "\\\"[^\\\"\n]+\\\"";
        String lazyClassRegexAutomaton = "\\\"[^\"\\\n]+?\\\"";
        String possessiveClassRegexAutomaton = "\\\"[^\\\"]++\\\"";

        for (int i = 0; i < NUM_WARM_UP_RUNS; i++) {
            runExperiment(lazyRegex, "LAZY REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(greedyClassRegex, "GREEDY WITH CLASS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lazyClassRegex, "LAZY WITH CLASS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveClassRegex, "POSSESSIVE WITH CLASS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(limitedPossessiveClassRegex, "LIMITED POSSESSIVE WITH CLASS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(backReferenceRegex, "BACK-REFERENCE REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            // runExperimentAutomaton(lazyRegex, "LAZY REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(greedyClassRegexAutomaton, "GREEDY WITH CLASS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(lazyClassRegexAutomaton, "LAZY WITH CLASS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(possessiveClassRegexAutomaton, "POSSESSIVE WITH CLASS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            // runExperimentAutomaton(limitedPossessiveClassRegex, "LIMITED POSSESSIVE WITH CLASS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            // runExperimentAutomaton(backReferenceRegex, "BACK-REFERENCE REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
        }
        write("**************END EXPERIMENT*****************\n\n");
    }

    private static void singleQuotesExperiment() {
        write("**************BEGIN SINGLE QUOTES EXPERIMENT *****************");
        // String simpleRegex = "'{2,5}.+?'{2,5}+";
        // String simpleRegex2 = "('{2,5}).+?\\1";

        // More complex regex to find nested quotes
        String greedyRegex = "('{2,5}).+?[^']\\1[^']";
        String lookAheadRegex = "('{2,5}).+?[^']\\1(?!')"; // The best
        String lookBehindRegex = "('{2,5}).+?(?<!')\\1[^']";
        String lookAheadBehindRegex = "('{2,5}).+?(?<!')\\1(?!')";
        String lookStarRegex = "('{2,5}).*?(?<!')\\1(?!')";

        for (int i = 0; i < NUM_WARM_UP_RUNS; i++) {
            // runExperiment(simpleRegex, "SIMPLE REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            // runExperiment(simpleRegex2, "SIMPLE REGEX 2", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(greedyRegex, "GREEDY REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadRegex, "LOOK AHEAD REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookBehindRegex, "LOOK BEHIND REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadBehindRegex, "LOOK AHEAD BEHIND REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookStarRegex, "LOOK STAR REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            // runExperimentAutomaton(simpleRegex, "SIMPLE REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            // runExperimentAutomaton(simpleRegex2, "SIMPLE REGEX 2", mediumText, i == NUM_WARM_UP_RUNS - 1);
            // runExperimentAutomaton(greedyRegex, "GREEDY REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            // runExperimentAutomaton(lookAheadRegex, "LOOK AHEAD REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            // runExperimentAutomaton(lookBehindRegex, "LOOK BEHIND REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            // runExperimentAutomaton(lookAheadBehindRegex, "LOOK AHEAD BEHIND REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            // runExperimentAutomaton(lookStarRegex, "LOOK STAR REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
        }
        write("**************END EXPERIMENT*****************\n\n");
    }

    private static void templateNameExperiment() {
        System.out.println("BEGIN TEMPLATE EXPERIMENT...");

        String regex1 = "\\{\\{[^|}:]+";
        String regex2 = "\\{\\{[^|}:]+?"; // Only works with Automaton
        String regex2B = "\\{\\{[^|}:]+?[|}:]";
        String regex2C = "\\{\\{[^|}:]+?(?=[|}:])"; // Only works with standard regex
        String regex3 = "\\{\\{[^|}:]++";

        for (int i = 1; i <= NUM_WARM_UP_RUNS; i++) {
            boolean isRealRun = (i == NUM_WARM_UP_RUNS);

            // The text-directed ones are quite faster. We test only them.
            //runExperiment(regex1, "1", isRealRun, false);
            runExperiment(regex1, "1", isRealRun, true);
            runExperiment(regex2, "2", isRealRun, true);
            //runExperiment(regex2B, "2B", isRealRun, false);
            runExperiment(regex2B, "2B", isRealRun, true);
            //runExperiment(regex2C, "2C", isRealRun, false);
            //runExperiment(regex3, "3", isRealRun, false);
            runExperiment(regex3, "3", isRealRun, true);
        }

        // The best ones are the regex2 variants. We choose regex2 as it is simpler.
    }

    private static void xmlTagExperiment() {
        System.out.println("BEGIN XML TAG EXPERIMENT...");

        String regex1 = "</?[A-z][\\p{L}\\p{N} =\"_-]+/?>";
        String regex1A = "\\</?[A-z](<L>|<N>|[ =\"_-])+/?\\>";
        String regex2 = "</?[A-z][\\p{L}\\p{N} =\"_-]+?/?>";
        String regex2A = "\\</?[A-z](<L>|<N>|[ =\"_-])+?/?\\>";
        String regex3 = "</?[A-z][\\p{L}\\p{N} =\"_-]+¡/?>";
        String regex3A = "\\</?[A-z](<L>|<N>|[ =\"_-])++/?\\>";

        for (int i = 1; i <= NUM_WARM_UP_RUNS; i++) {
            boolean isRealRun = (i == NUM_WARM_UP_RUNS);

            // The text-directed ones are quite faster. We test only them.
            //runExperiment(regex1, "1", isRealRun, false);
            runExperiment(regex1A, "1", isRealRun, true);
            //runExperiment(regex2, "2", isRealRun, false);
            runExperiment(regex2A, "2", isRealRun, true);
            //runExperiment(regex3, "3", isRealRun, false);
            runExperiment(regex3A, "3", isRealRun, true);
        }

        // For the standard regex there are no difference. We use regex2 for the escaped case.
        // For the automaton, all results are similar but the regex1 for long text. We take regex2A as the best average result.
    }

    private static void xmlEntityExperiment() {
        System.out.println("BEGIN XML ENTITY EXPERIMENT...");

        String regex1 = "&[a-z]+;";
        String regex2 = "&[a-z]+?;";
        String regex3 = "&[a-z]++;";

        for (int i = 1; i <= NUM_WARM_UP_RUNS; i++) {
            boolean isRealRun = (i == NUM_WARM_UP_RUNS);

            // The text-directed ones are quite faster. We test only them.
            //runExperiment(regex1, "1", isRealRun, false);
            runExperiment(regex1, "1", isRealRun, true);
            //runExperiment(regex2, "2", isRealRun, false);
            runExperiment(regex2, "2", isRealRun, true);
            //runExperiment(regex3, "3", isRealRun, false);
            runExperiment(regex3, "3", isRealRun, true);
        }

        // For the automaton, all results are similar but the regex1 for long text. We take regex2A as the best average result.
    }

    private static void completeTemplateExperiment() {
        System.out.println("BEGIN COMPLETE TEMPLATE EXPERIMENT...");

        String regex1 = "\\{\\{Cita\\|[^}]+";
        String regex2 = "\\{\\{Cita\\|[^}]+?"; // Don't work with standard regex
        String regex3 = "\\{\\{Cita\\|[^}]++";

        // Several template names: almost no difference, so we use this one.
        String regex4 = "\\{\\{(ORDENAR:|DEFAULTSORT:|NF\\||[Cc]ita\\||c?[Qq]uote\\||[Cc]oord\\||[Cc]ommonscat\\|)[^}]+?";

        // We try to capture nested templates, based on the best one (regex2) : 5% slower, it's worth !
        String regexTemplate = "\\{\\{[^}]+?}}";
        String regex5 = "\\{\\{(ORDENAR:|DEFAULTSORT:|NF\\||[Cc]ita\\||c?[Qq]uote\\||[Cc]oord\\||[Cc]ommonscat\\|)(" + regexTemplate + "|[^}])+?";
        // We try to close the template: no difference
        String regex6 = "\\{\\{(ORDENAR:|DEFAULTSORT:|NF\\||[Cc]ita\\||c?[Qq]uote\\||[Cc]oord\\||[Cc]ommonscat\\|)(" + regexTemplate + "|[^}])+?}}";

        for (int i = 1; i <= NUM_WARM_UP_RUNS; i++) {
            boolean isRealRun = (i == NUM_WARM_UP_RUNS);

            // The automaton is always the faster option, we test only these options.
            //runExperiment(regex1, "1", isRealRun, false);
            runExperiment(regex1, "1", isRealRun, true);
            runExperiment(regex2, "2", isRealRun, true);
            //runExperiment(regex3, "3", isRealRun, false);
            runExperiment(regex3, "3", isRealRun, true);

            runExperiment(regex4, "4", isRealRun, true);

            runExperiment(regex5, "5", isRealRun, true);
            runExperiment(regex6, "6", isRealRun, true);
        }
    }

    private static void indexValueExperiment() {
        System.out.println("BEGIN INDEX VALUE EXPERIMENT...");

        String regex1 = "\\|\\s*índice\\s*=[^}|]+";
        String regex1A = "\\|<Z>*índice<Z>*=[^}|]+";
        //String regex2 = "\\|\\s*índice\\s*=[^}|]+?";
        String regex2A = "\\|<Z>*índice<Z>*=[^}|]+?";
        String regex3 = "\\|\\s*índice\\s*=[^}|]++";
        String regex3A = "\\|<Z>*índice<Z>*=[^}|]++";

        // The look-behind to skip the parameter name only works in standard regex so we skip it
        // Based on the best (regex2) we try another regex with several parameter names
        String regex4A = "\\|<Z>*(índice|index)<Z>*=[^}|]+?";
        //String regex5A = "\\|<Z>*(?:índice|index)<Z>*=[^}|]+?";
        // There is almost no difference so we use regex4

        for (int i = 1; i <= NUM_WARM_UP_RUNS; i++) {
            boolean isRealRun = (i == NUM_WARM_UP_RUNS);

            // The automaton is always the faster option, we test only these options.
            // runExperiment(regex1, "1", isRealRun, false);
            runExperiment(regex1A, "1", isRealRun, true);
            runExperiment(regex2A, "2", isRealRun, true);
            // runExperiment(regex3, "3", isRealRun, false);
            runExperiment(regex3A, "3", isRealRun, true);

            runExperiment(regex4A, "4", isRealRun, true);
        }
    }

    private static void wordExperiment() {
        write("************** BEGIN WORD EXPERIMENT *****************");
        String letter = "A-Za-zÁÉÍÓÚÜáéíóúüÑñ";
        String simpleRegex = "\\b[" + letter + "][" + letter + "\\d]*\\b";
        String lazyRegex = "\\b[" + letter + "][" + letter + "\\d]*?\\b";
        String possessiveRegex = "\\b[" + letter + "][" + letter + "\\d]*+\\b";
        String endDigitRegex = "\\b[" + letter + "]++\\d?\\b";
        String unicodeRegex = "\\b\\p{L}++\\d?\\b";
        String fullUnicodeRegex = "\\b\\p{L}++\\p{N}?\\b";

        String matchingInput = ".....Águila2.....";
        String nonMatchingInput = ".....      .....";
        String almostMatchingInput = ".....1guila2.....";

        for (int i = 0; i < NUM_WARM_UP_RUNS; i++) {
            runExperiment(simpleRegex, "SIMPLE REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lazyRegex, "LAZY REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveRegex, "POSSESSIVE REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(endDigitRegex, "END DIGIT REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(unicodeRegex, "UNICODE REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(fullUnicodeRegex, "FULL UNICODE REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(simpleRegex, "SIMPLE REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lazyRegex, "LAZY REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveRegex, "POSSESSIVE REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(endDigitRegex, "END DIGIT REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(unicodeRegex, "UNICODE REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(fullUnicodeRegex, "FULL UNICODE REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(simpleRegex, "SIMPLE REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lazyRegex, "LAZY REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveRegex, "POSSESSIVE REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(endDigitRegex, "END DIGIT REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(unicodeRegex, "UNICODE REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(fullUnicodeRegex, "FULL UNICODE REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
        }
        write("**************END EXPERIMENT*****************\n\n");
    }

    private static void fileNameExperiment() {
        write("************** BEGIN FILE NAME EXPERIMENT *****************");

        String fileBrackets = "\\[\\[(File|Archivo|Imagen):[^]|]+]]";
        String fileDescBrackets = "\\[\\[(File|Archivo|Imagen):[^]|]+[]|]";
        String fileDescNoBrackets = "(File|Archivo|Imagen):[^]|]+[]|]";
        String fileDescNoBracketsLine = "(File|Archivo|Imagen):[^]|\n]+";
        String fileAlone = "[|=][^}|=\n]+\\.(gif|jpe?g|JPG|mp3|mpg|ogg|ogv|pdf|PDF|png|PNG|svg|tif|webm)";
        String fileAll = "[|=:][^}|=:\n]+\\.(gif|jpe?g|JPG|mp3|mpg|ogg|ogv|pdf|PDF|png|PNG|svg|tif|webm)";

        for (int i = 0; i < NUM_WARM_UP_RUNS; i++) {
            runExperiment(fileBrackets, "BRACKETS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(fileDescBrackets, "DESC REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(fileDescNoBrackets, "DESC NO BRACKETS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(fileDescNoBracketsLine, "DESC NO BRACKETS LINE REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(fileAlone, "ALONE REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(fileAll, "ALL REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperimentAutomaton(fileBrackets, "BRACKETS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(fileDescBrackets, "DESC REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(fileDescNoBrackets, "DESC NO BRACKETS REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(fileDescNoBracketsLine, "DESC NO BRACKETS LINE REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(fileAlone, "ALONE REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(fileAll, "ALL REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
        }
        write("************** END EXPERIMENT *****************\n\n");
    }

    private static void urlExperiment() {
        write("************** BEGIN URL EXPERIMENT *****************");

        String atEnd = "[^]\\s.:;,<>\"|)]";
        String inside = "[^]\\s<>\"|]";
        String regexUrl = "http[s]?://" + inside + "*" + atEnd;

        String domain = "\\w+\\.(com|org|es|net|gov|edu|gob|info)";
        String domainIgnored = "\\w+\\.(?:com|org|es|net|gov|edu|gob|info)";
        String domainLimited = "\\b\\w+\\.(?:com|org|es|net|gov|edu|gob|info)\\b";
        String domainLimited2 = "\\w+\\.(com|org|es|net|gov|edu|gob|info)[^\\.]";
        String domainAutomaton = "<L>+\\.(com|org|es|net|gov|edu|gob|info)";
        String domainLimitedAutomaton = "[^<L>]<L>+\\.(com|org|es|net|gov|edu|gob|info)[^\\.]";

        for (int i = 0; i < NUM_WARM_UP_RUNS; i++) {
            runExperiment(regexUrl, "REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(domain, "DOMAIN REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(domainIgnored, "DOMAIN IGNORED REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(domainLimited, "DOMAIN LIMITED REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(domainLimited2, "DOMAIN LIMITED2 REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperimentAutomaton(regexUrl, "REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(domainAutomaton, "DOMAIN REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(domainLimitedAutomaton, "DOMAIN LIMITED REGEX", mediumText, i == NUM_WARM_UP_RUNS - 1);
        }
        write("************** END EXPERIMENT *****************\n\n");
    }

    private static void completeTagExperiment() {
        System.out.println("BEGIN COMPLETE TAG EXPERIMENT...");

        // As the dot in Automaton is really greedy we cannot use it if we want to catch nested tags
        // I cannot find a different way to do it with the Automaton. The regex10 does not work.
        //String regex1 = "(?s)<math[^>]*>.+</math>";
        String regex2 = "(?s)<math[^>]*?>.+?</math>";
        //String regex3 = "(?s)<math[^>]*?>.++</math>";
        String regex4 = "(?s)<math[^>]*+>.+?</math>";
        //String regex5 = "(?s)<math[^>]*+>.++</math>";
        //String regex6 = "(?s)<math.+</math>";
        String regex7 = "(?s)<math.+?</math>";
        //String regex8 = "(?s)<math.++</math>";

        // Based in the winner we try with a back-reference
        String regex9 = "(?s)<(math)[^>]*+>.+?</\\1>";

        // We try another approach to the nested tags
        String regex10 = "<(math)(<[^>]+>|[^<])+?</\\1>";

        for (int i = 1; i <= NUM_WARM_UP_RUNS; i++) {
            boolean isRealRun = (i == NUM_WARM_UP_RUNS);

            runExperiment(regex2, "2", isRealRun, false);
            runExperiment(regex4, "4", isRealRun, false);
            runExperiment(regex7, "7", isRealRun, false);

            runExperiment(regex9, "9", isRealRun, false);
            runExperiment(regex10, "10", isRealRun, false);
        }

        // The three regex are similar. The regex4 is a little better for medium and long text and regex7 for longest text.
        // We choose regex4 for the general case, and regex7 for the escaped one.
        // The back-reference regex9 is 50% slower than regex4 => Winner for more than one tag name
    }

    private static void templateParamExperiment() {
        System.out.println("BEGIN TEMPLATE PARAMETER EXPERIMENT...");

        String regex1 = "\\|\\s*[\\p{L}\\p{N} _-]+\\s*=";
        String regex1A = "\\|\\s*(<L>|<N>|[ _-])+\\s*=";
        String regex2 = "\\|[\\p{L}\\p{N}\\s_-]+=";
        String regex2A = "\\|(<L>|<N>|<Z>|[_-])+=";

        String regex3 = "\\|\\s*[\\p{L}\\p{N} _-]+?\\s*=";
        String regex3A = "\\|\\s*(<L>|<N>|[ _-])+?\\s*=";
        String regex4 = "\\|[\\p{L}\\p{N}\\s_-]+?=";
        String regex4A = "\\|(<L>|<N>|<Z>|[_-])+?=";

        String regex5 = "\\|\\s*[\\p{L}\\p{N} _-]++\\s*=";
        String regex5A = "\\|\\s*(<L>|<N>|[ _-])++\\s*=";
        String regex6 = "\\|[\\p{L}\\p{N}\\s_-]++=";
        String regex6A = "\\|(<L>|<N>|<Z>|[_-])++=";

        // Based on the best: regex3
        String regex7 = "\\|\\s*[\\p{L}\\p{N} _-]+?\\s*(?==)";

        for (int i = 1; i <= NUM_WARM_UP_RUNS; i++) {
            boolean isRealRun = (i == NUM_WARM_UP_RUNS);

            runExperiment(regex1, "1", isRealRun, false);
            runExperiment(regex1A, "1", isRealRun, true);
            runExperiment(regex2, "2", isRealRun, false);
            runExperiment(regex2A, "2", isRealRun, true);

            runExperiment(regex3, "3", isRealRun, false);
            runExperiment(regex3A, "3", isRealRun, true);
            runExperiment(regex4, "4", isRealRun, false);
            runExperiment(regex4A, "4", isRealRun, true);

            runExperiment(regex5, "5", isRealRun, false);
            runExperiment(regex5A, "5", isRealRun, true);
            runExperiment(regex6, "6", isRealRun, false);
            runExperiment(regex6A, "6", isRealRun, true);

            runExperiment(regex7, "7", isRealRun, false);
        }

        // All times are quite similar. The regex3 is a little faster.
        // It is not worth to use the look-ahead (regex7) not to capture the =
    }

    private static void runExperiment(String regex, String regexDescription, String input, boolean printOutput) {
        Pattern p = Pattern.compile(regex);

        boolean matches = false;
        Long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_RUNS; i++) {
            Matcher matcher = p.matcher(input);
            while (matcher.find()) {
                matches = true;
            }
        }
        Long timeElapsed = System.currentTimeMillis() - start;

        if (printOutput) {
            String result = String.format(
                    "Took %d ms to match the %s regex with %s input",
                    timeElapsed, regexDescription, matches ? "matching" : "NON matching");
            write(result);
        }
    }

    private static void runExperimentAutomaton(String regex, String regexDescription, String input, boolean printOutput) {
        RegExp r = new RegExp(regex);
        Automaton a = r.toAutomaton(new DatatypesAutomatonProvider());
        RunAutomaton ra = new RunAutomaton(a);

        boolean matches = false;
        Long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_RUNS; i++) {
            AutomatonMatcher matcher = ra.newMatcher(input);
            while (matcher.find()) {
                matches = true;
            }
        }
        Long timeElapsed = System.currentTimeMillis() - start;

        if (printOutput) {
            String result = String.format(
                    "Took %d ms to match the %s regex with %s input",
                    timeElapsed, regexDescription, matches ? "matching" : "NON matching");
            write(result);
        }
    }

    private static void write(String s) {
        System.out.println(s);
    }

    private static void runExperiment(String regex, String regexId, boolean isRealRun, boolean isTextDirected) {
        runExperiment(regex, mediumText, "medium", regexId, isRealRun, isTextDirected);
        runExperiment(regex, longText, "long", regexId, isRealRun, isTextDirected);
        runExperiment(regex, longestText, "longest", regexId, isRealRun, isTextDirected);
        if (isRealRun) {
            System.out.println();
        }
    }

    private static void runExperiment(String regex, String text, String textType, String regexId, boolean isRealRun,
                                      boolean isTextDirected) {
        Matcher matcher = null;
        AutomatonMatcher automatonMatcher = null;

        // Text-directed
        if (isTextDirected) {
            RegExp r = new RegExp(regex);
            Automaton a = r.toAutomaton(new DatatypesAutomatonProvider());
            RunAutomaton ra = new RunAutomaton(a);
            automatonMatcher = ra.newMatcher(text);
        } else {
            // Regex-directed
            Pattern pattern = Pattern.compile(regex);
            matcher = pattern.matcher(text);
        }

        long start = System.currentTimeMillis();
        for (int j = 1; j <= NUM_RUNS; j++) {
            if (isTextDirected) {
                while (automatonMatcher.find()) {
                    // Do nothing
                }
            } else { // Regex directed
                while (matcher.find()) {
                    // Do nothing
                }
            }
        }
        long timeElapsed = System.currentTimeMillis() - start;
        if (isRealRun) {
            System.out.println("Regex " + regexId + ": " + timeElapsed + " ms with " + textType + " text " + (isTextDirected ? "(text-directed)" : ""));
        }
    }

}
