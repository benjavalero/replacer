package es.bvalero.replacer.date;

import es.bvalero.replacer.finder.ReplacementFinder;

import java.util.Arrays;
import java.util.List;
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

}
