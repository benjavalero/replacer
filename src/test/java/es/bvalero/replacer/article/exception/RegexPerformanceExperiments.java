package es.bvalero.replacer.article.exception;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
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
    private static String articleText = null;

    static {
        try {
            articleText = new String(Files.readAllBytes(Paths.get(RegexPerformanceExperiments.class.getResource("/monkey-island.txt").toURI())),
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
        // completeTemplateExperiment();
        // indexValueExperiment();
        // wordExperiment();
        fileNameExperiment();
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
            runExperiment(alternationsRegex, "ALTERNATIONS REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(alternationsIgnoredRegex, "IGNORED REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(alternationsBracketRegex, "BRACKET REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(alternationsBoundIgnoredRegex, "BOUND IGNORED REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(alternationsBoundRegex, "BOUND REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperimentAutomaton(alternationsRegex, "ALTERNATIONS REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(alternationsIgnoredRegex, "IGNORED REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(alternationsBracketRegex, "BRACKET REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(alternationsBoundIgnoredRegex, "BOUND IGNORED REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(alternationsBoundRegex, "BOUND REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
        }
        write("**************END EXPERIMENT*****************\n\n");
    }

    private static void angularQuotesExperiment() {
        write("**************BEGIN ANGULAR QUOTES EXPERIMENT *****************");
        String lazyRegex = "(?s)«.+?»";
        String greedyClassRegex = "«[^»]+»";
        String lazyClassRegex = "«[^»]+?»";
        String possessiveClassRegex = "«[^»]++»"; // Wins in all the cases
        String conditionalRegex = "(?:(«)|“).++(?(1)»|”)";

        String matchingInput = "Quote: «What a Wonderful World».";
        String nonMatchingInput = "Quote: What a Wonderful World.";
        String almostMatchingInput = "Quote: «What a Wonderful World.";

        for (int i = 0; i < NUM_WARM_UP_RUNS; i++) {
            runExperiment(lazyRegex, "LAZY REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(greedyClassRegex, "GREEDY WITH CLASS REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lazyClassRegex, "LAZY WITH CLASS REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveClassRegex, "POSSESSIVE WITH CLASS REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(conditionalRegex, "CONDITIONAL REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(lazyRegex, "LAZY REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(greedyClassRegex, "GREEDY WITH CLASS REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lazyClassRegex, "LAZY WITH CLASS REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveClassRegex, "POSSESSIVE WITH CLASS REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(conditionalRegex, "CONDITIONAL REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(lazyRegex, "LAZY REGEX (almost matching)", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(greedyClassRegex, "GREEDY WITH CLASS REGEX (almost matching)", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lazyClassRegex, "LAZY WITH CLASS REGEX (almost matching)", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveClassRegex, "POSSESSIVE WITH CLASS REGEX (almost matching)", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(conditionalRegex, "CONDITIONAL REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
        }
        write("**************END EXPERIMENT*****************\n\n");
    }

    private static void doubleQuotesExperiment() {
        write("**************BEGIN DOUBLE QUOTES EXPERIMENT *****************");
        String lazyRegex = "(?s)\".+?\"";
        // Discard the rest of cases according to the angular quotes experiment
        String possessiveClassRegex = "\"[^\"]++\"";
        String backReferenceRegex = "(?s)(\"|&quot;).+?\\1"; // It takes a little more but it is actually two (or more) regex in one

        String matchingInput = "Quote: \"What a Wonderful World\".";
        String nonMatchingInput = "Quote: What a Wonderful World.";
        String almostMatchingInput = "Quote: \"What a Wonderful World.";

        for (int i = 0; i < NUM_WARM_UP_RUNS; i++) {
            runExperiment(lazyRegex, "LAZY REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveClassRegex, "POSSESSIVE WITH CLASS REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(backReferenceRegex, "BACK-REFERENCE REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(lazyRegex, "LAZY REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveClassRegex, "POSSESSIVE WITH CLASS REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(backReferenceRegex, "BACK-REFERENCE REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(lazyRegex, "LAZY REGEX (almost matching)", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveClassRegex, "POSSESSIVE WITH CLASS REGEX (almost matching)", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(backReferenceRegex, "BACK-REFERENCE REGEX (almost matching)", matchingInput, i == NUM_WARM_UP_RUNS - 1);
        }
        write("**************END EXPERIMENT*****************\n\n");
    }

    private static void singleQuotesExperiment() {
        write("**************BEGIN SINGLE QUOTES EXPERIMENT *****************");
        String simpleRegex = "(('|&apos;){2,5}+|\"|&quot;).+?\\1";
        String simpleRegex2 = "('{2,5}+|&apos;{2,5}+|\"|&quot;).+?\\1";

        // More complex regex to find nested quotes
        String greedyRegex = "('{2,5}).+?[^']\\1[^']";
        String lookAheadRegex = "('{2,5}).+?[^']\\1(?!')"; // The best
        String lookBehindRegex = "('{2,5}).+?(?<!')\\1[^']";
        String lookAheadBehindRegex = "('{2,5}).+?(?<!')\\1(?!')";
        String lookStarRegex = "('{2,5}).*?(?<!')\\1(?!')";
        String possessiveRegex = "('{2,5}+).+?[^']\\1(?!')"; // Based in the look-ahead regex, which seems the best.

        String matchingInput = "Quote: '''What a Wonderful World'''.";
        String nonMatchingInput = "Quote: What a Wonderful World.";
        String almostMatchingInput = "Quote: '''What a Wonderful World.";

        for (int i = 0; i < NUM_WARM_UP_RUNS; i++) {
            runExperiment(simpleRegex, "SIMPLE REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(simpleRegex2, "SIMPLE REGEX 2", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(greedyRegex, "GREEDY REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadRegex, "LOOK AHEAD REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookBehindRegex, "LOOK BEHIND REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadBehindRegex, "LOOK AHEAD BEHIND REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookStarRegex, "LOOK STAR REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveRegex, "POSSESSIVE REGEX", matchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(simpleRegex, "SIMPLE REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(simpleRegex2, "SIMPLE REGEX 2", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(greedyRegex, "GREEDY REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadRegex, "LOOK AHEAD REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookBehindRegex, "LOOK BEHIND REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadBehindRegex, "LOOK AHEAD BEHIND REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookStarRegex, "LOOK STAR REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveRegex, "POSSESSIVE REGEX", nonMatchingInput, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperiment(simpleRegex, "SIMPLE REGEX", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(simpleRegex2, "SIMPLE REGEX 2", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(greedyRegex, "GREEDY REGEX (almost matching)", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadRegex, "LOOK AHEAD REGEX (almost matching)", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookBehindRegex, "LOOK BEHIND REGEX (almost matching)", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookAheadBehindRegex, "LOOK AHEAD BEHIND REGEX (almost matching)", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(lookStarRegex, "LOOK STAR REGEX (almost matching)", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(possessiveRegex, "POSSESSIVE REGEX (almost matching)", almostMatchingInput, i == NUM_WARM_UP_RUNS - 1);
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
            runExperiment(fileBrackets, "BRACKETS REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(fileDescBrackets, "DESC REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(fileDescNoBrackets, "DESC NO BRACKETS REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(fileDescNoBracketsLine, "DESC NO BRACKETS LINE REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(fileAlone, "ALONE REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperiment(fileAll, "ALL REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);

            if (i == NUM_WARM_UP_RUNS - 1) {
                System.out.println();
            }

            runExperimentAutomaton(fileBrackets, "BRACKETS REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(fileDescBrackets, "DESC REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(fileDescNoBrackets, "DESC NO BRACKETS REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(fileDescNoBracketsLine, "DESC NO BRACKETS LINE REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(fileAlone, "ALONE REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
            runExperimentAutomaton(fileAll, "ALL REGEX", articleText, i == NUM_WARM_UP_RUNS - 1);
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
        Automaton a = r.toAutomaton();
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
}
