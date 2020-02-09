package es.bvalero.replacer.finder.benchmark;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

abstract class ParameterValueAbstractFinder {

    static final List<String> PARAMS = Arrays.asList("índice", "index", "cita", "location", "ubicación");

    abstract Set<FinderResult> findMatches(String text);

}
