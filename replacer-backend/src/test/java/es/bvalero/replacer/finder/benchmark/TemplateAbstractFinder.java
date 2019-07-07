package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;
import es.bvalero.replacer.finder.ReplacementFinder;

import java.util.Set;

abstract class TemplateAbstractFinder extends ReplacementFinder {

    abstract Set<MatchResult> findMatches(String text);

}
