package es.bvalero.replacer.finder.benchmark;

import java.util.Set;

abstract class TemplateAbstractFinder {

    abstract Set<IgnoredReplacement> findMatches(String text);

}
