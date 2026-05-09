package io.github.lystrosaurus.atlasmountain.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "io.github.lystrosaurus.atlasmountain",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class LayerArchitectureTest {

    @ArchTest
    static final ArchRule controllers_do_not_depend_on_dao_mapper_or_entity =
            noClasses().that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAnyPackage("..dao..", "..mapper..", "..entity..");

    @ArchTest
    static final ArchRule services_do_not_depend_on_mappers =
            noClasses().that().resideInAPackage("..service..")
                    .should().dependOnClassesThat().resideInAPackage("..mapper..");

    @ArchTest
    static final ArchRule only_dao_impl_depends_on_mappers =
            noClasses().that().resideOutsideOfPackages("..dao.impl..", "..config..", "..mapper..")
                    .should().dependOnClassesThat().resideInAPackage("..mapper..");

    @ArchTest
    static final ArchRule controllers_do_not_depend_on_entity =
            noClasses().that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAPackage("..entity..");
}
