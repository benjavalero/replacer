package es.bvalero.replacer.date;

import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.BaseReplacementFinder;
import es.bvalero.replacer.finder.ReplacementSuggestion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

abstract class DateFinder extends BaseReplacementFinder {

    private static final String TYPE_DATE = "Fechas";

    private static final List<String> MONTHS = Arrays.asList(
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "sep?tiembre", "octubre", "noviembre", "diciembre");
    static final List<String> MONTHS_UPPERCASE = MONTHS.stream()
            .map(DateFinder::setFirstUpperCase)
            .collect(Collectors.toList());
    static final List<String> MONTHS_UPPERCASE_CLASS = MONTHS.stream()
            .map(DateFinder::setFirstUpperCaseClass)
            .collect(Collectors.toList());

    public List<Replacement> findReplacements(String text) {
        return findMatchResults(text, getAutomaton()).stream()
                .filter(match -> isWordCompleteInText(match.getStart(), match.getText(), text))
                .map(match -> convertMatchResultToReplacement(
                        match,
                        TYPE_DATE,
                        getSubType(),
                        findSuggestions(match.getText())))
                .collect(Collectors.toList());
    }

    abstract RunAutomaton getAutomaton();

    abstract String getSubType();

    private List<ReplacementSuggestion> findSuggestions(String date) {
        return Collections.singletonList(ReplacementSuggestion.ofNoComment(fixDate(date)));
    }

    private String fixDate(String date) {
        // Replace the uppercase months
        String fixedDate = date.replaceAll("[Ss]ep?tiembre", "septiembre");
        for (String month : MONTHS_UPPERCASE) {
            fixedDate = fixedDate.replaceAll(month, month.toLowerCase(Locale.forLanguageTag("es")));
        }

        // Replace "Del?" before year
        fixedDate = fixedDate.replaceAll("[Dd]el?(?= \\d)", "de");

        // Replace "De" after day
        fixedDate = fixedDate.replaceAll("(?<=\\d )De", "de");

        // Replace the leading zero
        if (fixedDate.startsWith("0")) {
            fixedDate = fixedDate.substring(1);
        }

        return fixedDate;
    }

}
