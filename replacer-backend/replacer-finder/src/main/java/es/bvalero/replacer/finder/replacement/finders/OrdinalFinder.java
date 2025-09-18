package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import com.roman.code.ConvertToRoman;
import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import javax.naming.OperationNotSupportedException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find ordinals to be corrected in Spanish, e.g. without the dot between the number and the symbol.
 */
@Component
class OrdinalFinder implements ReplacementFinder {

    private static final String MASCULINE_LETTER = "o";
    private static final String FEMININE_LETTER = "a";
    private static final String PLURAL_LETTER = "s";

    // Dependency injection
    private final FinderProperties finderProperties;

    private static final Set<String> MASCULINE_SUFFIXES = new HashSet<>();
    private static final Set<String> FEMININE_SUFFIXES = new HashSet<>();
    private static final Set<String> SUFFIXES = new HashSet<>();

    OrdinalFinder(FinderProperties finderProperties) {
        this.finderProperties = finderProperties;
    }

    @PostConstruct
    public void init() {
        // Load masculine and feminine suffixes
        MASCULINE_SUFFIXES.add(Character.toString(MASCULINE_ORDINAL));
        MASCULINE_SUFFIXES.add(Character.toString(DEGREE));
        MASCULINE_SUFFIXES.addAll(finderProperties.getOrdinalSuffixes().getMasculine());
        FEMININE_SUFFIXES.add(Character.toString(FEMININE_ORDINAL));
        FEMININE_SUFFIXES.addAll(finderProperties.getOrdinalSuffixes().getFeminine());

        // Merge all singular suffixes
        Stream.of(MASCULINE_SUFFIXES, FEMININE_SUFFIXES).flatMap(Collection::stream).forEach(SUFFIXES::add);

        // Add plural forms
        addPluralSuffixes();
    }

    private void addPluralSuffixes() {
        // Return plural forms (if applicable)
        Set<String> singularSuffixes = new HashSet<>(SUFFIXES); // Copy
        for (String singularSuffix : singularSuffixes) {
            String lastLetter = singularSuffix.substring(singularSuffix.length() - 1);
            if (lastLetter.equals(MASCULINE_LETTER) || lastLetter.equals(FEMININE_LETTER)) {
                SUFFIXES.add(singularSuffix + PLURAL_LETTER);
            }
        }
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        if (WikipediaLanguage.SPANISH == page.getPageKey().getLang()) {
            // The linear approach is about better than the automaton
            return LinearMatchFinder.find(page, this::findOrdinal);
        } else {
            return Stream.of();
        }
    }

    @Nullable
    private MatchResult findOrdinal(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final MatchResult matchNumber = findOrdinalNumber(text, start);
            if (matchNumber == null) {
                return null;
            }
            final int endNumber = matchNumber.end();

            // Find the suffix, right after the number.
            final MatchResult matchSuffix = findOrdinalSuffix(text, endNumber);
            if (matchSuffix == null) {
                start = endNumber;
                continue;
            }

            final int startOrdinal = matchNumber.start();
            final int endOrdinal = matchSuffix.end();
            final FinderMatchResult matchResult = FinderMatchResult.of(text, startOrdinal, endOrdinal);
            matchResult.addGroup(matchNumber);
            matchResult.addGroup(matchSuffix);
            return matchResult;
        }
        return null;
    }

    /* Find the next number candidate to be the number of an ordinal */
    @Nullable
    private MatchResult findOrdinalNumber(String text, int start) {
        while (start >= 0 && start < text.length()) {
            final MatchResult matchNumber = FinderUtils.findNumber(text, start, false, false);
            if (matchNumber == null) {
                return null;
            }

            if (isOrdinalNumber(matchNumber, text)) {
                return matchNumber;
            } else {
                start = matchNumber.end();
            }
        }
        return null;
    }

    /* Validate if the number is candidate to be the number of an ordinal */
    private boolean isOrdinalNumber(MatchResult numberMatch, String text) {
        // Max. 3 digits allowed
        // Not preceded by letter, e.g. a3o
        // At this point the number string should match an integer
        final String number = numberMatch.group();
        return (
            number.length() <= 3 &&
            Integer.parseInt(number) > 0 &&
            (numberMatch.start() == 0 || FinderUtils.isValidSeparator(text.charAt(numberMatch.start() - 1))) &&
            numberMatch.end() < text.length()
        );
    }

    /* Find an ordinal suffix right after the number match */
    @Nullable
    private MatchResult findOrdinalSuffix(String text, int start) {
        assert start < text.length();
        final MatchResult matchSuffix;
        if (isOrdinalLetter(text.charAt(start))) {
            matchSuffix = FinderMatchResult.of(start, text.substring(start, start + 1));
        } else {
            matchSuffix = FinderUtils.findWordAfter(text, start, true, DEGREE);
            // It must be a known suffix
            if (matchSuffix == null || !SUFFIXES.contains(matchSuffix.group())) {
                return null;
            }
        }

        // Suffix must be right after the number
        // We only allow an optional dot before
        // Exception: an ordinal symbol preceded by a dot must not be fixed
        final int startSuffix = matchSuffix.start();
        if (startSuffix > start + 1 || (startSuffix == start + 1 && text.charAt(start) != DOT)) {
            return null;
        }

        // Optional dot after the suffix
        // Always return the match starting right after the number
        int endSuffix = matchSuffix.end();
        if (endSuffix < text.length() && text.charAt(endSuffix) == DOT) {
            endSuffix += 1;
        }
        return FinderMatchResult.of(text, start, endSuffix);
    }

    private boolean isOrdinalLetter(char ch) {
        // The degree symbol is not an ordinal letter but in this case it applies
        return ch == MASCULINE_ORDINAL || ch == FEMININE_ORDINAL;
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        // Not to be fixed:
        // - dot + ordinal symbol
        // - degree symbol
        final String suffix = ((FinderMatchResult) match).group(2);
        return switch (suffix.length()) {
            case 2 -> suffix.charAt(0) != DOT || !isOrdinalLetter(suffix.charAt(1));
            case 1 -> suffix.charAt(0) != DEGREE;
            default -> true;
        };
    }

    @Override
    public Replacement convert(MatchResult match, FinderPage page) {
        return Replacement.of(match.start(), match.group(), StandardType.ORDINAL, buildSuggestions(match, page));
    }

    private List<Suggestion> buildSuggestions(MatchResult match, FinderPage page) {
        final List<Suggestion> suggestions = new ArrayList<>();

        // Split the number and the suffix
        final int ordinalNumber = Integer.parseInt(match.group(1));
        final String ordinalSuffix = stripSuffix(match.group(2));

        // 1. Fixed ordinal
        // Including the feminine fix only for masculine ordinals
        suggestions.addAll(buildFixedOrdinalSuggestions(ordinalNumber, ordinalSuffix));

        // 2. Cardinal
        suggestions.add(buildCardinalSuggestion(ordinalNumber));

        // 3. Degrees (only for masculine ordinals)
        CollectionUtils.addIgnoreNull(suggestions, buildDegreeSuggestion(ordinalNumber, ordinalSuffix));

        // 4. Roman number
        suggestions.add(buildRomanNumberSuggestion(ordinalNumber));

        // 5. Text alternatives (if any)
        final WikipediaLanguage lang = page.getPageKey().getLang();
        suggestions.addAll(buildTextSuggestions(ordinalNumber, ordinalSuffix, lang));

        return suggestions;
    }

    /* Remove the initial/final dot */
    private String stripSuffix(String suffix) {
        return StringUtils.strip(suffix, String.valueOf(DOT));
    }

    private boolean isOrdinalPlural(String suffix) {
        return suffix.endsWith(PLURAL_LETTER);
    }

    /* Remove the plural form */
    private String getSingularSuffix(String suffix) {
        return isOrdinalPlural(suffix) ? StringUtils.chop(suffix) : suffix;
    }

    private char getOrdinalSymbol(String suffix) {
        // The suffix is stripped but can be plural
        final String bareSuffix = getSingularSuffix(suffix);
        if (FEMININE_SUFFIXES.contains(bareSuffix)) {
            return FEMININE_ORDINAL;
        } else if (MASCULINE_SUFFIXES.contains(bareSuffix)) {
            return MASCULINE_ORDINAL;
        } else {
            throw new IllegalArgumentException("Unknown suffix: " + suffix);
        }
    }

    private String getOrdinalLetter(char ordinalSymbol) {
        return ordinalSymbol == FEMININE_ORDINAL ? FEMININE_LETTER : MASCULINE_LETTER;
    }

    private Collection<Suggestion> buildFixedOrdinalSuggestions(int ordinalNumber, String ordinalSuffix) {
        final List<Suggestion> suggestions = new ArrayList<>();

        // In case of a masculine ordinal, we include also the fix in feminine.
        final char ordinalSymbol = this.getOrdinalSymbol(ordinalSuffix);
        final boolean isOrdinalMasculine = (ordinalSymbol == MASCULINE_ORDINAL);
        final boolean isOrdinalPlural = isOrdinalPlural(ordinalSuffix);
        if (isOrdinalMasculine) {
            suggestions.add(buildFixedOrdinalSuggestion(ordinalNumber, true, isOrdinalPlural));
        }
        suggestions.add(buildFixedOrdinalSuggestion(ordinalNumber, false, isOrdinalPlural));

        return suggestions;
    }

    private Suggestion buildFixedOrdinalSuggestion(
        int ordinalNumber,
        boolean isOrdinalMasculine,
        boolean isOrdinalPlural
    ) {
        final char ordinalSymbol = isOrdinalMasculine ? MASCULINE_ORDINAL : FEMININE_ORDINAL;
        final String ordinalLetter = getOrdinalLetter(ordinalSymbol);
        if (isOrdinalPlural) {
            return Suggestion.ofNoComment("{{ord|" + ordinalNumber + ".|" + ordinalLetter + PLURAL_LETTER + "}}");
        } else {
            return Suggestion.of(
                ordinalNumber + "." + ordinalSymbol,
                "se escribe punto antes de la " + ordinalLetter + " volada"
            );
        }
    }

    private Suggestion buildCardinalSuggestion(int ordinalNumber) {
        return Suggestion.of(Integer.toString(ordinalNumber), "cardinal");
    }

    @Nullable
    private Suggestion buildDegreeSuggestion(int ordinalNumber, String ordinalSuffix) {
        final char ordinalSymbol = this.getOrdinalSymbol(ordinalSuffix);
        final boolean isOrdinalMasculine = (ordinalSymbol == MASCULINE_ORDINAL);
        return isOrdinalMasculine ? Suggestion.of(Integer.toString(ordinalNumber) + DEGREE, "grados") : null;
    }

    private Suggestion buildRomanNumberSuggestion(int ordinalNumber) {
        try {
            return Suggestion.of(ConvertToRoman.fromArabic(ordinalNumber), "números romanos");
        } catch (OperationNotSupportedException e) {
            throw new IllegalStateException("Ordinal number not convertible into Roman: " + ordinalNumber, e);
        }
    }

    private Collection<Suggestion> buildTextSuggestions(
        int ordinalNumber,
        String ordinalSuffix,
        WikipediaLanguage lang
    ) {
        final List<Suggestion> suggestions = new ArrayList<>();

        final FinderProperties.OrdinalSuggestion ordinalSuggestion =
            this.finderProperties.getOrdinalSuggestions().get(lang.getCode()).get(ordinalNumber);
        if (ordinalSuggestion == null) {
            return suggestions;
        }

        final char ordinalSymbol = this.getOrdinalSymbol(ordinalSuffix);
        final String ordinalLetter = this.getOrdinalLetter(ordinalSymbol);
        final boolean isOrdinalMasculine = (ordinalSymbol == MASCULINE_ORDINAL);
        final boolean isOrdinalPlural = isOrdinalPlural(ordinalSuffix);

        // Ordinal
        final FinderProperties.OrdinalOption ordinalOption = ordinalSuggestion.getOrdinal();
        final List<String> ordinalOptions = isOrdinalMasculine
            ? ordinalOption.getMasculine()
            : ordinalOption.getFeminine();
        if (isOrdinalPlural) {
            ordinalOptions
                .stream()
                .filter(opt -> opt.endsWith(ordinalLetter))
                .map(opt -> opt + PLURAL_LETTER)
                .map(Suggestion::ofNoComment)
                .forEach(suggestions::add);
        } else {
            ordinalOptions.stream().map(Suggestion::ofNoComment).forEach(suggestions::add);
        }

        // Fractional
        final FinderProperties.OrdinalOption fractionalOption = ordinalSuggestion.getFractional();
        if (fractionalOption != null) {
            final List<String> fractionalOptions = isOrdinalMasculine
                ? fractionalOption.getMasculine()
                : fractionalOption.getFeminine();
            if (isOrdinalPlural) {
                fractionalOptions
                    .stream()
                    .filter(opt -> opt.endsWith(ordinalLetter))
                    .map(opt -> opt + PLURAL_LETTER)
                    .map(s -> Suggestion.of(s, "fraccionario"))
                    .forEach(suggestions::add);
            } else {
                fractionalOptions.stream().map(s -> Suggestion.of(s, "fraccionario")).forEach(suggestions::add);
            }
        }

        return suggestions;
    }
}
