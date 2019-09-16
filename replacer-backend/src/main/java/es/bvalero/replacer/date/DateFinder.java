package es.bvalero.replacer.date;

import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.finder.ReplacementSuggestion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

abstract class DateFinder extends ReplacementFinder {

    static final String TYPE_DATE = "Fechas";
    static final String SUBTYPE_DATE_UPPERCASE_MONTHS = "Mes en mayúscula";

    private static final List<String> MONTHS = Arrays.asList(
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "sep?tiembre", "octubre", "noviembre", "diciembre");
    static final List<String> MONTHS_UPPERCASE = MONTHS.stream()
            .map(DateFinder::setFirstUpperCase)
            .collect(Collectors.toList());
    static final List<String> MONTHS_UPPERCASE_CLASS = MONTHS.stream()
            .map(DateFinder::setFirstUpperCaseClass)
            .collect(Collectors.toList());

    List<ReplacementSuggestion> findSuggestions(String date) {
        return Collections.singletonList(ReplacementSuggestion.ofNoComment(fixDate(date)));
    }

    private String fixDate(String date) {
        // Replace the uppercase months
        String fixedDate = date.replaceAll("[Ss]ep?tiembre", "septiembre");
        for (String month : MONTHS_UPPERCASE) {
            fixedDate = fixedDate.replaceAll(month, month.toLowerCase(Locale.forLanguageTag("es")));
        }

        // Replace "del"
        fixedDate = fixedDate.replace("del", "de");

        // Replace the leading zero
        if (fixedDate.startsWith("0")) {
            fixedDate = fixedDate.substring(1);
        }

        return fixedDate;
    }

}
