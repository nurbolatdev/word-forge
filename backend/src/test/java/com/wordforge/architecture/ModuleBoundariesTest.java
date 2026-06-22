package com.wordforge.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.wordforge", importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleBoundariesTest {
    private static final String[] FEATURE_MODULES = {
            "..analytics..",
            "..enrichment..",
            "..identity..",
            "..lists..",
            "..quiz..",
            "..scheduler..",
            "..translation..",
            "..vocabulary.."
    };

    @ArchTest
    static final ArchRule featureModulesDoNotDependOnEachOther = noClasses()
            .that()
            .resideInAnyPackage(FEATURE_MODULES)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(FEATURE_MODULES)
            .because("stage 0 keeps feature modules isolated; shared contracts belong in common");
}
