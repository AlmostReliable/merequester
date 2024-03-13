@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.api.LoomGradleExtensionAPI

val license: String by project
val minecraftVersion: String by project
val modVersion: String by project
val modPackage: String by project
val modId: String by project
val modName: String by project
val modAuthor: String by project
val modDescription: String by project
val parchmentVersion: String by project
val neoforgeVersion: String by project
val forgeRecipeViewer: String by project
val aeVersion: String by project
val jeiVersion: String by project
val reiVersion: String by project
val githubUser: String by project
val githubRepo: String by project

plugins {
    id("dev.architectury.loom") version "1.4-SNAPSHOT"
    id("com.github.gmazzo.buildconfig") version "4.0.4"
    java
}

base {
    version = "$minecraftVersion-$modVersion"
    group = modPackage
    archivesName.set("$modId-forge")
}

loom {
    silentMojangMappingsLicense()
}

repositories {
    maven("https://maven.parchmentmc.org/") // Parchment
    maven("https://maven.neoforged.net/releases") // Parchment
    maven("https://modmaven.dev/") // Applied Energistics 2
    maven("https://maven.blamejared.com") // JEI
    maven("https://maven.shedaniel.me") // REI
    mavenLocal()
}

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
    })

    // Forge
    neoForge("net.neoforged:neoforge:$neoforgeVersion")

    // Compile
    modCompileOnly("appeng:appliedenergistics2-neoforge:$aeVersion")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api:$reiVersion")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-neoforge:$reiVersion")

    // Runtime
    modLocalRuntime("dev.architectury:architectury-neoforge:11.1.17") // TODO: Remove on new REI version
    modLocalRuntime("appeng:appliedenergistics2-neoforge:$aeVersion")
    when (forgeRecipeViewer) {
        "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-neoforge:$reiVersion")
        "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-neoforge:$jeiVersion") { isTransitive = false }
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
            "neoforgeVersion" to neoforgeVersion,
            "forgeLoaderVersion" to "1",
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

extensions.configure<LoomGradleExtensionAPI> {
    runs {
        forEach {
            it.vmArgs("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
        }
    }
}

buildConfig {
    buildConfigField("String", "MOD_ID", "\"$modId\"")
    buildConfigField("String", "MOD_NAME", "\"$modName\"")
    buildConfigField("String", "MOD_VERSION", "\"$version\"")
    packageName(modPackage)
    useJavaOutput()
}
