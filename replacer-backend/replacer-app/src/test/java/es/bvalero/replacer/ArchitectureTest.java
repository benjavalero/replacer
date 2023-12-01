package es.bvalero.replacer;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;

@AnalyzeClasses(packages = "es.bvalero.replacer")
class ArchitectureTest {

    // TODO: All core use cases must implement an interface which will belong to the domain module
    // TODO: Adapter modules (in particular, Web) will only depend on domain module
    // TODO: We don't need architecture tests anymore

    @ArchTest
    ArchRule hexagonal = JMoleculesArchitectureRules.ensureHexagonal();
}
