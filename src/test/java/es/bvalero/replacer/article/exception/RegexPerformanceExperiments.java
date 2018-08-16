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
    private static final int NUM_RUNS = 2500;
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
        angularQuotesExperiment();
        // doubleQuotesExperiment();
        // singleQuotesExperiment();
        // templateNameExperiment();
        // xmlTagExperiment();
        // completeTemplateExperiment();
        // indexValueExperiment();
        // wordExperiment();
        // fileNameExperiment();
        // urlExperiment();
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

        //String regex1 = "(?s)«.+»";
        String regex2 = "(?s)«.+?»";
        //String regex3 = "(?s)«.++»";
        String regex4 = "«[^»]+»";
        String regex5 = "«[^»]+?»";
        String regex6 = "«[^»]++»";

        for (int i = 1; i <= NUM_WARM_UP_RUNS; i++) {
            boolean isRealRun = (i == NUM_WARM_UP_RUNS);

            runExperiment(regex2, "2", isRealRun, false);
            runExperiment(regex4, "4", isRealRun, false);
            runExperiment(regex4, "4", isRealRun, true);
            runExperiment(regex5, "5", isRealRun, false);
            runExperiment(regex5, "5", isRealRun, true);
            runExperiment(regex6, "6", isRealRun, false); // Winner
            runExperiment(regex6, "6", isRealRun, true);
        }
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
        write("**************BEGIN TEMPLATE NAME EXPERIMENT *****************");
        String greedyRegex = "\\{\\{[^|}]+";
        String lazyRegex = "\\{\\{[^|}]+?[|}]"; // We need to match the end
        String lazyRegex2 = "\\{\\{[^|}]+?(?=[|}])"; // We need to match the end
        String possessiveRegex = "\\{\\{[^|}]++";
        String lookBehindRegex = "(?<=\\{\\{)[^|}]+";

        String matchingInput = "Template: {{ Template | Content }}.";
        String nonMatchingInput = "Template: { Template | Content }.";
        String almostMatchingInput = "Template: {{ Template - Content .";

        for (int i = 0; i < NUM_WARM_UP_RUNS; i++) {
            runExperiment(greedyRegex, "GREEDY REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lazyRegex, "LAZY REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lazyRegex2, "LAZY REGEX 2", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveRegex, "POSSESSIVE REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookBehindRegex, "LOOK BEHIND REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(greedyRegex, "GREEDY REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lazyRegex, "LAZY REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lazyRegex2, "LAZY REGEX 2", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveRegex, "POSSESSIVE REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookBehindRegex, "LOOK BEHIND REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(greedyRegex, "GREEDY REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lazyRegex, "LAZY REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lazyRegex2, "LAZY REGEX 2", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveRegex, "POSSESSIVE REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookBehindRegex, "LOOK BEHIND REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
        }
        write("**************END EXPERIMENT*****************\n\n");
    }

    private static void xmlTagExperiment() {
        write("**************BEGIN XML TAG EXPERIMENT *****************");
        String negativeRegex = "<\\w[^>]++>";
        String lookAheadRegex = "<(?!!)[^>]++>";
        String classRegex = "<[\\wÁáÉéÍíÓóÚúÜüÑñ\\-\\s=\"/]++>";

        String matchingInput = "Ref: <ref name=\"Hola\" />.";
        String nonMatchingInput = "Ref: ref name=\"Hola\".";
        String almostMatchingInput = "Ref: <ref name=\"Hola\" /.";

        for (int i = 0; i < NUM_WARM_UP_RUNS; i++) {
            runExperiment(negativeRegex, "NEGATIVE REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadRegex, "LOOK-AHEAD REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(classRegex, "CLASS REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(negativeRegex, "NEGATIVE REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadRegex, "LOOK-AHEAD REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(classRegex, "CLASS REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(negativeRegex, "NEGATIVE REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadRegex, "LOOK-AHEAD REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(classRegex, "CLASS REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
        }
        write("**************END EXPERIMENT*****************\n\n");
    }

    private static void completeTemplateExperiment() {
        write("**************BEGIN COMPLETE TEMPLATE EXPERIMENT *****************");
        String templateRegex = "\\{\\{[^}]++}}";
        String simpleRegex = "\\{\\{Quote\\|[^}]++}}";
        String nestedRegex = "\\{\\{Quote\\|(" + templateRegex + "|[^}])++}}";

        String matchingInput = "Quote: {{Quote|What a Wonderful World}}.";
        String nonMatchingInput = "Quote: What a Wonderful World.";
        String almostMatchingInput = "Quote: {{Quote|What a Wonderful World.";

        for (int i = 0; i < NUM_WARM_UP_RUNS; i++) {
            runExperiment(simpleRegex, "SIMPLE REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(nestedRegex, "NESTED REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(simpleRegex, "SIMPLE REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(nestedRegex, "NESTED REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(simpleRegex, "SIMPLE REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(nestedRegex, "NESTED REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
        }
        write("**************END EXPERIMENT*****************\n\n");
    }

    private static void indexValueExperiment() {
        write("************** BEGIN INDEX VALUE EXPERIMENT *****************");
        String simpleRegex = "\\|\\s*índice\\s*=[^}|]*";
        String possessiveRegex = "\\|\\s*índice\\s*=[^}|]*+";
        String lookBehindRegex = "\\|\\s*índice\\s*=.+?(?=[}|])";
        String lookAheadRegex = "(?<=\\|)\\s*índice\\s*=[^}|]*+";

        String matchingInput = "Quote: {{Quote| índice = What a Wonderful World}}.";
        String nonMatchingInput = "Quote: {{Quote índice = What a Wonderful World}}.";
        String almostMatchingInput = "Quote: {{Quote| índice = What a Wonderful World.";

        for (int i = 0; i < NUM_WARM_UP_RUNS; i++) {
            runExperiment(simpleRegex, "SIMPLE REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveRegex, "POSSESSIVE REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookBehindRegex, "LOOK BEHIND REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadRegex, "LOOK AHEAD REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(simpleRegex, "SIMPLE REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveRegex, "POSSESSIVE REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookBehindRegex, "LOOK BEHIND REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadRegex, "LOOK AHEAD REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(simpleRegex, "SIMPLE REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveRegex, "POSSESSIVE REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookBehindRegex, "LOOK BEHIND REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadRegex, "LOOK AHEAD REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
        }
        write("**************END EXPERIMENT*****************\n\n");
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
        // Regex-directed
        Pattern pattern = Pattern.compile(regex);
        // Text-directed
        RegExp r = new RegExp(regex);
        Automaton a = r.toAutomaton(new DatatypesAutomatonProvider());
        RunAutomaton ra = new RunAutomaton(a);

        long start = System.currentTimeMillis();
        for (int j = 1; j <= 2000; j++) {
            if (isTextDirected) {
                AutomatonMatcher matcher = ra.newMatcher(text);
                while (matcher.find()) {
                    // Do nothing
                }
            } else { // Regex directed
                Matcher matcher = pattern.matcher(text);
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
