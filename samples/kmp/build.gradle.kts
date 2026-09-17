plugins {
    id("com.android.application").version("8.8.2").apply(false)
    id("com.android.library").version("8.8.2").apply(false)
    kotlin("android").version("2.1.0").apply(false)
    kotlin("plugin.compose").version("2.1.0").apply(false)
    kotlin("multiplatform").version("2.1.0").apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    // !! infrastructure for builds as a Kotlin user project
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach kotlinCompilationTask@ {
        // ("unconditionally pass the -Xreport-all-warnings CLI option to the Kotlin compiler")
        // ("unconditionally pass the -Xrender-internal-diagnostic-names CLI option to the Kotlin compiler")
        compilerOptions {
            // output reported warnings even in the presence of reported errors
            freeCompilerArgs.add("-Xreport-all-warnings")
            logger.info("<KUP> ${this@kotlinCompilationTask.path} : added -Xreport-all-warnings")
            // output kotlin.git-searchable names of reported diagnostics
            freeCompilerArgs.add("-Xrender-internal-diagnostic-names")
            logger.info("<KUP> ${this@kotlinCompilationTask.path} : added -Xrender-internal-diagnostic-names")
        }
    }

// !! infrastructure for builds as a Kotlin user project
// ("implement a Gradle property for overriding LVs when compiling Kotlin code")
    val kotlinLanguageVersionOverride = providers.gradleProperty("kotlin_language_version")
        .map(org.jetbrains.kotlin.gradle.dsl.KotlinVersion::fromVersion)
        .orNull
    if (kotlinLanguageVersionOverride != null) {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach kotlinCompilationTask@ {
            compilerOptions {
                languageVersion.set(kotlinLanguageVersionOverride)
                logger.info("<KUP> ${this@kotlinCompilationTask.path} : set LV to $kotlinLanguageVersionOverride")
            }
        }
    }

// !! infrastructure for builds as a Kotlin user project
// ("implement a Gradle property for overriding APIVs when compiling Kotlin code")
    val kotlinApiVersionOverride = providers.gradleProperty("kotlin_api_version")
        .map(org.jetbrains.kotlin.gradle.dsl.KotlinVersion::fromVersion)
        .orNull
    if (kotlinApiVersionOverride != null) {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach kotlinCompilationTask@ {
            compilerOptions {
                apiVersion.set(kotlinApiVersionOverride)
                logger.info("<KUP> ${this@kotlinCompilationTask.path} : set APIV to $kotlinApiVersionOverride")
            }
        }
    }

// !! infrastructure for builds as a Kotlin user project
// ("implement a Gradle property for passing additional CLI options to the Kotlin compiler")
    val kotlinAdditionalCliOptions = providers.gradleProperty("kotlin_additional_cli_options")
        .map { if (it.isNotBlank()) it.split(" ") else emptyList() } // i sure hope that this level of parsing is in fact enough
        .orNull
    if (kotlinAdditionalCliOptions != null) {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach kotlinCompilationTask@ {
            compilerOptions {
                freeCompilerArgs.addAll(kotlinAdditionalCliOptions)
                logger.info("<KUP> ${this@kotlinCompilationTask.path} : added ${kotlinAdditionalCliOptions.joinToString(" ")}")
            }
        }
    }

// !! infrastructure for builds as a Kotlin user project
// ("unconditionally disable the -Werror compiler CLI option")
    afterEvaluate {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach kotlinCompilationTask@ {
            compilerOptions {
                allWarningsAsErrors.set(false)
                freeCompilerArgs.set(
                    freeCompilerArgs.getOrElse(emptyList()).filterNot { it == "-Werror" }
                )
                logger.info("<KUP> ${this@kotlinCompilationTask.path} : disabled -Werror")
            }
        }
    }
}