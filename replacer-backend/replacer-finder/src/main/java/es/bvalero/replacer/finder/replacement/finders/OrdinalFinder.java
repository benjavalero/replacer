package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import com.roman.code.ConvertToRoman;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.naming.OperationNotSupportedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Find ordinals to be corrected in Spanish, e.g. without the dot between the number and the symbol.
 */
@Component
public class OrdinalFinder implements ReplacementFinder {

    private static final String MASCULINE_LETTER = "o";
    private static final String FEMININE_LETTER = "a";
    private static final String PLURAL_LETTER = "s";

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_NUMBER = "[1-9][0-9]{0,2}";

    @Autowired
    private FinderProperties finderProperties;

    private RunAutomaton ordinalAutomaton;

    private static final Set<String> MASCULINE_SUFFIXES = new HashSet<>();
    private static final Set<String> FEMININE_SUFFIXES = new HashSet<>();

    @PostConstruct
    public void init() {
        // Load masculine and feminine suffixes
        MASCULINE_SUFFIXES.add(Character.toString(MASCULINE_ORDINAL));
        MASCULINE_SUFFIXES.addAll(finderProperties.getOrdinalSuffixes().getMasculine());
        FEMININE_SUFFIXES.add(Character.toString(FEMININE_ORDINAL));
        FEMININE_SUFFIXES.addAll(finderProperties.getOrdinalSuffixes().getFeminine());

        // Merge all singular suffixes
        Set<String> suffixes = Stream
            .of(MASCULINE_SUFFIXES, FEMININE_SUFFIXES)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        // Add plural forms
        suffixes.addAll(getPluralSuffixes(suffixes));

        String ordinalRegex = String.format("%s(%s)\\.?", REGEX_NUMBER, FinderUtils.joinAlternate(suffixes));
        this.ordinalAutomaton =
            new RunAutomaton(new RegExp(ordinalRegex).toAutomaton(new DatatypesAutomatonProvider()));
    }

    private Collection<String> getPluralSuffixes(Collection<String> singularSuffixes) {
        // Return plural forms (if applicable)
        Set<String> pluralSuffixes = new HashSet<>();
        for (String singularSuffix : singularSuffixes) {
            String lastLetter = singularSuffix.substring(singularSuffix.length() - 1);
            if (lastLetter.equals(MASCULINE_LETTER) || lastLetter.equals(FEMININE_LETTER)) {
                pluralSuffixes.add(singularSuffix + PLURAL_LETTER);
            }
        }
        return pluralSuffixes;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        if (WikipediaLanguage.SPANISH == page.getPageKey().getLang()) {
            // The linear approach is about 5x better than the automaton
            // However we use the automaton approach to include more cases
            return AutomatonMatchFinder.find(page.getContent(), ordinalAutomaton);
        } else {
            return List.of();
        }
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return isWordCompleteInText(match.start(), match.group(), page.getContent());
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

    private List<Suggestion> buildSuggestions(MatchResult match, FinderPage page) {
        final List<Suggestion> suggestions = new ArrayList<>();

        // Split the number and the suffix
        final String ordinal = match.group();
        final int startSuffix = findStartSuffix(ordinal);
        final int ordinalNumber = Integer.parseInt(ordinal.substring(0, startSuffix));

        // We store the pure suffix, removing the final dot or the plural form.
        String suffixStr = ordinal.substring(startSuffix);
        if (suffixStr.endsWith(".")) {
            suffixStr = StringUtils.chop(suffixStr);
        }
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

    private int findStartSuffix(String ordinal) {
        // To find the suffix start, we assume all characters at the left of the result are digits.
        int startSuffix = 0;
        while (startSuffix < ordinal.length()) {
            if (!Character.isDigit(ordinal.charAt(startSuffix))) {
                break;
            } else {
                startSuffix++;
            }
        }
        assert startSuffix >= 1;
        return startSuffix;
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
