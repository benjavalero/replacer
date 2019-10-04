package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;
import es.bvalero.replacer.finder.BaseReplacementFinder;

import java.util.Set;

abstract class TemplateAbstractFinder extends BaseReplacementFinder {

    abstract Set<IgnoredReplacement> findMatches(String text);

}
