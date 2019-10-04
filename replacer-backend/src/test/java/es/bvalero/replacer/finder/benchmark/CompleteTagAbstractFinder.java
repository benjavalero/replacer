package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;

import java.util.Set;

abstract class CompleteTagAbstractFinder {

    abstract Set<IgnoredReplacement> findMatches(String text);

}
