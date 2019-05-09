package es.bvalero.replacer.misspelling.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.Set;

abstract class UppercaseAbstractFinder {

    abstract Set<MatchResult> findMatches(String text);

}
