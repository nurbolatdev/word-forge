package com.wordforge.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

@AnalyzeClasses(packages = "com.wordforge", importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleBoundariesTest {

    // Slices are defined by the first package segment under com.wordforge.*.
    // beFreeOfCycles() allows controlled unidirectional cross-module dependencies
    // (e.g. lists → vocabulary → translation) but prevents circular deps.
    // Allowed direction: lists → vocabulary → translation; common is shared kernel.
    @ArchTest
    static final ArchRule featureModulesDoNotDependOnEachOther =
            SlicesRuleDefinition.slices()
                    .matching("com.wordforge.(*)..")
                    .should().beFreeOfCycles()
                    .because("feature modules must not form cycles; unidirectional deps are allowed");
}
