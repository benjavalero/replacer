package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;

import java.util.Set;

abstract class CategoryAbstractFinder {

    abstract Set<IgnoredReplacement> findMatches(String text);

}
