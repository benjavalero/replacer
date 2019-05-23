package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

abstract class ParameterValueAbstractFinder {

    final static List<String> PARAMS = Arrays.asList("índice", "index", "cita", "location", "ubicación");

    abstract Set<MatchResult> findMatches(String text);

}
