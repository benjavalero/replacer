package es.bvalero.replacer.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

abstract class ParameterValueAbstractFinder {

    static final List<String> PARAMS = Arrays.asList("índice", "index", "cita", "location", "ubicación");

    abstract Set<IgnoredReplacement> findMatches(String text);

}
