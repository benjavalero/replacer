package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import com.roman.code.ConvertToRoman;
import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.naming.OperationNotSupportedException;
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
    private static final Set<Character> SUFFIX_ALLOWED_CHARS = Set.of(DEGREE);

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
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        if (WikipediaLanguage.SPANISH == page.getPageKey().getLang()) {
            // The linear approach is about better than the automaton
            return LinearMatchFinder.find(page, this::findOrdinal);
        } else {
            return List.of();
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

    @Nullable
    private MatchResult findOrdinalNumber(String text, int start) {
        while (start >= 0 && start < text.length()) {
            final MatchResult matchNumber = FinderUtils.findNumber(text, start, false, false);
            if (matchNumber == null) {
                return null;
            }

            // Validate here the number to skip unnecessary steps
            if (!isOrdinalNumber(matchNumber.group())) {
                start = matchNumber.end();
                continue;
            }

            return matchNumber;
        }
        return null;
    }

    private boolean isOrdinalNumber(String number) {
        try {
            return number.length() <= 3 && Integer.parseInt(number) > 0;
        } catch (NumberFormatException nfe) {
            throw new IllegalStateException(nfe);
        }
    }

    @Nullable
    private MatchResult findOrdinalSuffix(String text, int start) {
        // Find the suffix, right after the number.
        if (start >= text.length()) {
            return null;
        }

        final MatchResult matchSuffix;
        if (isOrdinalLetter(text.charAt(start))) {
            matchSuffix = FinderMatchResult.of(start, text.substring(start, start + 1));
        } else {
            matchSuffix = FinderUtils.findWordAfter(text, start, SUFFIX_ALLOWED_CHARS, true);
        }

        if (matchSuffix == null || !SUFFIXES.contains(matchSuffix.group())) {
            return null;
        }

        // Suffix must be right after the number
        // We only allow an optional dot before
        if (matchSuffix.start() > start + 1 || (matchSuffix.start() == start + 1 && text.charAt(start) != DOT)) {
            return null;
        }

        // Optional dot after the suffix
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
        final String suffix = ((FinderMatchResult) match).group(2);
        return switch (suffix.length()) {
            case 2 -> suffix.charAt(0) != DOT || !isOrdinalLetter(suffix.charAt(1));
            case 1 -> suffix.charAt(0) != DEGREE;
            default -> true;
        };
    }

    @Override
    public Replacement convert(MatchResult match, FinderPage page) {
        return Replacement
            .builder()
            .page(page)
            .type(StandardType.ORDINAL)
            .start(match.start())
            .text(match.group())
            .suggestions(buildSuggestions(match, page))
            .build();
    }

    // TODO: Reduce cyclomatic complexity
    private List<Suggestion> buildSuggestions(MatchResult match, FinderPage page) {
        final List<Suggestion> suggestions = new ArrayList<>();

        // Split the number and the suffix
        final int ordinalNumber = Integer.parseInt(match.group(1));

        // We store the pure suffix, removing the initial/final dot or the plural form.
        String suffixStr = StringUtils.strip(match.group(2), String.valueOf(DOT));
        final boolean isOrdinalPlural = suffixStr.endsWith(PLURAL_LETTER);
        if (isOrdinalPlural) {
            suffixStr = StringUtils.chop(suffixStr);
        }
        final String ordinalSuffix = suffixStr;

        // Calculate the ordinal symbol and the letter
        final char ordinalSymbol = this.getOrdinalSymbol(ordinalSuffix);
        final String ordinalLetter = this.getOrdinalLetter(ordinalSuffix);
        final boolean isOrdinalMasculine = (ordinalSymbol == MASCULINE_ORDINAL);

        // Dot + original letter
        if (isOrdinalPlural) {
            suggestions.add(
                Suggestion.ofNoComment(String.format("{{ord|%d.|%s%s}}", ordinalNumber, ordinalLetter, PLURAL_LETTER))
            );
        } else {
            suggestions.add(
                Suggestion.of(
                    ordinalNumber + "." + ordinalSymbol,
                    "se escribe punto antes de la " + ordinalLetter + " volada"
                )
            );
        }

        // Cardinal
        suggestions.add(Suggestion.of(Integer.toString(ordinalNumber), "cardinal"));

        // Degrees (only for masculine ordinals)
        if (isOrdinalMasculine) {
            suggestions.add(Suggestion.of(Integer.toString(ordinalNumber) + DEGREE, "grados"));
        }

        // Roman
        try {
            suggestions.add(Suggestion.of(ConvertToRoman.fromArabic(ordinalNumber), "números romanos"));
        } catch (OperationNotSupportedException e) {
            // Simply don't add this alternative
            throw new IllegalStateException(e);
        }

        // Text alternatives (if any)
        String lang = page.getPageKey().getLang().getCode();
        final FinderProperties.OrdinalSuggestion ordinalSuggestion =
            this.finderProperties.getOrdinalSuggestions().get(lang).get(ordinalNumber);
        if (ordinalSuggestion != null) {
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
        }

        return suggestions;
    }

    private char getOrdinalSymbol(String suffix) {
        if (FEMININE_SUFFIXES.contains(suffix)) {
            return FEMININE_ORDINAL;
        } else if (MASCULINE_SUFFIXES.contains(suffix)) {
            return MASCULINE_ORDINAL;
        } else {
            throw new IllegalArgumentException("Unknown suffix: " + suffix);
        }
    }

    private String getOrdinalLetter(String suffix) {
        return getOrdinalSymbol(suffix) == FEMININE_ORDINAL ? FEMININE_LETTER : MASCULINE_LETTER;
    }
}
