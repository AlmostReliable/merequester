@file:Suppress("UnstableApiUsage")

val license: String by project
val minecraftVersion: String by project
val modVersion: String by project
val modPackage: String by project
val modId: String by project
val modName: String by project
val modAuthor: String by project
val modDescription: String by project
val forgeVersion: String by project
val forgeRecipeViewer: String by project
val aeVersion: String by project
val jeiVersion: String by project
val reiVersion: String by project
val githubUser: String by project
val githubRepo: String by project

plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.140"
    id("com.github.gmazzo.buildconfig") version "4.0.4"
    java
}

// cannot be configured inside the block for some reason
legacyForge.version = "$minecraftVersion-$forgeVersion"

base {
    version = "$minecraftVersion-$modVersion"
    archivesName.set("$modId-forge")
}

legacyForge {
    runs {
        configureEach {
            // DCEVM hot-swapping
            jvmArgument("-XX:+AllowEnhancedClassRedefinition")
            jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
        }

        create("client") {
            client()
        }
        create("server") {
            server()
        }
    }
    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

mixin {
    add(sourceSets.main.get(), "$modId.mixins.refmap.json")
    config("$modId.mixins.json")
}

repositories {
    maven("https://modmaven.dev/") // Applied Energistics 2
    maven("https://maven.blamejared.com") // JEI
    maven("https://maven.shedaniel.me") // REI
}

dependencies {
    // Mixin
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    // Compile
    modCompileOnly("appeng:appliedenergistics2-forge:$aeVersion")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-forge:$reiVersion")

    // Runtime
    modRuntimeOnly("appeng:appliedenergistics2-forge:$aeVersion")
    when (forgeRecipeViewer) {
        "rei" -> modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-forge:$reiVersion")
        "jei" -> modRuntimeOnly("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion") { isTransitive = false }
        else -> throw GradleException("Invalid recipeViewer value: $forgeRecipeViewer")
    }
}

tasks {
    processResources {
        val resourceTargets = listOf("META-INF/mods.toml", "pack.mcmeta")

        val replaceProperties = mapOf(
            "license" to license,
            "minecraftVersion" to minecraftVersion,
            "version" to project.version as String,
            "modId" to modId,
            "modName" to modName,
            "modAuthor" to modAuthor,
            "modDescription" to modDescription,
            "forgeVersion" to forgeVersion,
            "forgeLoaderVersion" to forgeVersion.substringBefore("."),
            "aeVersion" to aeVersion,
            "githubUser" to githubUser,
            "githubRepo" to githubRepo
        )

        println("[Process Resources] Replacing properties in resources: ")
        replaceProperties.forEach { (key, value) -> println("\t -> $key = $value") }

        inputs.properties(replaceProperties)
        filesMatching(resourceTargets) {
            expand(replaceProperties)
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    withType<GenerateModuleMetadata> {
        enabled = false
    }
}

extensions.configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

buildConfig {
    buildConfigField("String", "MOD_ID", "\"$modId\"")
    buildConfigField("String", "MOD_NAME", "\"$modName\"")
    buildConfigField("String", "MOD_VERSION", "\"$version\"")
    packageName(modPackage)
    useJavaOutput()
}
