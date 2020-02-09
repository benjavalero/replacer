package es.bvalero.replacer.finder.benchmark;

import java.util.Set;

abstract class UppercaseAbstractFinder {

    abstract Set<IgnoredReplacement> findMatches(String text);

}
