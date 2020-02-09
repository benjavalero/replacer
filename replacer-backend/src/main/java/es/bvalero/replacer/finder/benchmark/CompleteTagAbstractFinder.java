package es.bvalero.replacer.finder.benchmark;

import java.util.Set;

abstract class CompleteTagAbstractFinder {

    abstract Set<IgnoredReplacement> findMatches(String text);

}
