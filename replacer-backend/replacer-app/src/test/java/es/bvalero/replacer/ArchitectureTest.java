package es.bvalero.replacer;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;

@AnalyzeClasses(packages = "es.bvalero.replacer")
class ArchitectureTest {

    @ArchTest
    ArchRule hexagonal = JMoleculesArchitectureRules.ensureHexagonal();
}
