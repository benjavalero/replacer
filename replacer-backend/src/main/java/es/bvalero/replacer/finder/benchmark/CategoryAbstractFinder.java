package es.bvalero.replacer.finder.benchmark;

import java.util.Set;

abstract class CategoryAbstractFinder {

    abstract Set<IgnoredReplacement> findMatches(String text);

}
