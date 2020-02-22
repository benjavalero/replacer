package es.bvalero.replacer.finder.benchmark.domain;

import java.util.Set;

abstract class DomainFinder {

    abstract Set<String> findMatches(String text);
}
