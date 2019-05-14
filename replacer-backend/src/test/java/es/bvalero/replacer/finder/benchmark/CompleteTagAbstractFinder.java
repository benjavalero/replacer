package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.Set;

abstract class CompleteTagAbstractFinder {

    abstract Set<MatchResult> findMatches(String text);

}
