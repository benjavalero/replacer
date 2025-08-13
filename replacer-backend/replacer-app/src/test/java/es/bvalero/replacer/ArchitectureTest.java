package es.bvalero.replacer;

import com.tngtech.archunit.core.domain.JavaClass;
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
        ApplicationModules modules = buildApplicationModules();
        modules.forEach(System.out::println);
    }

    @Test
    void verifiesModuleStructure() {
        ApplicationModules modules = buildApplicationModules();
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        ApplicationModules modules = buildApplicationModules();
        new Documenter(modules).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml();
    }

    private ApplicationModules buildApplicationModules() {
        return ApplicationModules.of(
            Replacer.class,
            JavaClass.Predicates.resideInAPackage("es.bvalero.replacer.common..")
        );
    }
}
