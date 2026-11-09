import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.shizuku.refine)
    alias(libs.plugins.gradle.toolchains) apply false
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
}

android {
    namespace = "com.machiav3lli.fdroid"
    compileSdk = 37
    compileSdkMinor = 1

    defaultConfig {
        applicationId = "com.droidlite.client"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0-droidlite"

        // Droid-Lite: trimmed locale set (trade-off documented in PLAN.md).
        // Cuts resources.arsc from ~2 MB to a fraction; upstream Neo Store ships 62.
        resourceConfigurations += listOf(
            "en", "de", "es", "fr", "it", "pt-rBR", "pt-rPT", "ru", "uk",
            "pl", "nl", "tr", "ar", "zh-rCN", "zh-rTW", "ja", "ko", "hi", "id",
        )
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        buildConfig = true
        compose = true
        resValues = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            resValue("string", "application_name", "Droid-Lite Debug")
        }
        register("neo") {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".neo"
            resValue("string", "application_name", "Droid-Lite Neo")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            resValue("string", "application_name", "Droid-Lite")
        }
        all {
            isCrunchPngs = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard.pro"
            )
        }
    }
    packaging {
        jniLibs {
            excludes += listOf("/okhttp3/internal/publicsuffix/*")
        }
        resources {
            excludes += listOf(
                "/DebugProbesKt.bin",
                "/kotlin/**.kotlin_builtins",
                "/kotlin/**.kotlin_metadata",
                "/META-INF/**.kotlin_module",
                "/META-INF/**.pro",
                "/META-INF/**.version", // comment out to enable layout inspector
                "META-INF/*.LICENSE.txt",
                "META-INF/DEPENDENCIES",
                "META-INF/versions/9/OSGI-INF/MANIFEST.MF",
                "PublicSuffixDatabase.list",
            )
        }
    }

    lint {
        disable += "InvalidVectorPath"
        warning += "InvalidPackage"
    }

    dependenciesInfo {
        // Avoid Google-signed dependency metadata in builds
        includeInApk = false
        includeInBundle = false
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    val generateLocales by tasks.registering(GenerateBuildConfig::class) {
        resDir.set(project.layout.projectDirectory.dir("src/main/res"))
        outputDir.set(project.layout.buildDirectory.dir("generated/source/locales/kotlin/main"))
    }

    androidComponents.onVariants { variant ->
        variant.sources.kotlin!!.addGeneratedSourceDirectory(
            generateLocales,
            GenerateBuildConfig::outputDir
        )

        tasks.withType<KotlinCompile> {
            dependsOn(generateLocales)
        }
    }
    androidComponents.onVariants { variant ->
        variant.outputs.forEach { output ->
            if (output is com.android.build.api.variant.impl.VariantOutputImpl) {
                output.outputFileName.set(
                    "Droid_Lite_${output.versionName.get()}_${variant.buildType}.apk"
                )
            }
        }
    }
}

dependencies {
    // Core
    implementation(libs.kotlin.stdlib)
    implementation(libs.ksp)
    implementation(libs.activity.compose)
    implementation(libs.collections.immutable)
    coreLibraryDesugaring(libs.jdk.desugar)
    //debugImplementation(libs.leakcanary)

    implementation(libs.work.runtime)

    // Coil, OkHttp
    api(platform(libs.coil.bom))
    implementation(libs.coil)
    implementation(libs.coil.okhttp)
    implementation(libs.coil.compose)
    implementation(libs.okhttp)

    // Koin
    api(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.workmanager)
    implementation(libs.koin.compose)
    implementation(libs.koin.startup)

    // JSON, LibSu, Shizuku
    implementation(libs.libsu.core)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.shizuku.refine)
    compileOnly(libs.shizuku.hidden)
    implementation(libs.hiddenapi.bypass)
    implementation(libs.serialization.json)

    // Coroutines / Lifecycle
    implementation(libs.lifecycle)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Compose
    api(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.animation)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // Test
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
}

abstract class GenerateBuildConfig : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:IgnoreEmptyDirectories
    abstract val resDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        group = "build"
        description = "Generates BuildConfig.kt from auto-fetched values"
    }

    @TaskAction
    fun generate() {
        val detectedLocales = mutableSetOf<String>()
        resDir.get().asFileTree.visit {
            if (file.isFile && file.name == "strings.xml" && file.readText().contains("<string")) {
                val languageCode = file.parentFile?.name?.removePrefix("values-")?.let {
                    if (it == "values") "en" else it.removePrefix("b+").replace("+", "-").replace("-r", "-")
                }
                languageCode?.let { detectedLocales.add(it) }
            }
        }

        val outputFile =
            outputDir.file("com/machiav3lli/fdroid/config/BuildConfig.kt").get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            package com.machiav3lli.fdroid.config
            
            object BuildConfig {
                val DETECTED_LOCALES: Array<String> = arrayOf(${
                detectedLocales.sorted().joinToString { "\"$it\"" }
            })
            }
        """.trimIndent()
        )

        println("BuildConfig: Generated ${detectedLocales.size} locales to ${outputFile.absolutePath}")
    }
}