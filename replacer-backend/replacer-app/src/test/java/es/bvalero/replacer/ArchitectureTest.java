package es.bvalero.replacer;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@AnalyzeClasses(packages = "es.bvalero.replacer")
class ArchitectureTest {

    @ArchTest
    ArchRule hexagonal = JMoleculesArchitectureRules.ensureHexagonal();

    @Test
    void createApplicationModuleModel() {
        ApplicationModules modules = ApplicationModules.of(Replacer.class);
        modules.forEach(System.out::println);
    }

    @Test
    void verifiesModuleStructure() {
        ApplicationModules modules = ApplicationModules.of(Replacer.class);
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        ApplicationModules modules = ApplicationModules.of(Replacer.class);
        new Documenter(modules).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml();
    }
}
