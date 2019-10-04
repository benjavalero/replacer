package es.bvalero.replacer.date;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.ReplacementSuggestion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

abstract class DateFinder {

    private static final String TYPE_DATE = "Fechas";

    private static final List<String> MONTHS = Arrays.asList(
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "sep?tiembre", "octubre", "noviembre", "diciembre");
    static final List<String> MONTHS_UPPERCASE = MONTHS.stream()
            .map(FinderUtils::setFirstUpperCase)
            .collect(Collectors.toList());
    static final List<String> MONTHS_UPPERCASE_CLASS = MONTHS.stream()
            .map(FinderUtils::setFirstUpperCaseClass)
            .collect(Collectors.toList());

    public String getType() {
        return TYPE_DATE;
    }

    public List<ReplacementSuggestion> findSuggestions(String date) {
        return Collections.singletonList(ReplacementSuggestion.ofNoComment(fixDate(date)));
    }

    private String fixDate(String date) {
        // Replace the uppercase months
        String fixedDate = date.replaceAll("[Ss]ep?tiembre", "septiembre");
        for (String month : MONTHS_UPPERCASE) {
            fixedDate = fixedDate.replaceAll(month, FinderUtils.toLowerCase(month));
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
