package es.bvalero.replacer.misspelling.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;

import java.util.Set;

abstract class UppercaseAbstractFinder {

    abstract Set<IgnoredReplacement> findMatches(String text);

}
