package com.wordforge.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

@AnalyzeClasses(packages = "com.wordforge", importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleBoundariesTest {

    // Slices are defined by the first package segment under com.wordforge.*
    // (analytics, enrichment, identity, lists, quiz, scheduler, translation, vocabulary, common).
    // notDependOnEachOther() allows intra-slice dependencies and only forbids
    // one slice importing from a different slice.
    @ArchTest
    static final ArchRule featureModulesDoNotDependOnEachOther =
            SlicesRuleDefinition.slices()
                    .matching("com.wordforge.(*)..")
                    .should().notDependOnEachOther()
                    .because("feature modules must be isolated; shared contracts belong in common");
}
