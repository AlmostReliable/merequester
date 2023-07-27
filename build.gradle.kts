@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.api.LoomGradleExtensionAPI

val license: String by project
val enableAccessWidener: String by project
val minecraftVersion: String by project
val modVersion: String by project
val modPackage: String by project
val modId: String by project
val modName: String by project
val modAuthor: String by project
val modDescription: String by project
val parchmentVersion: String by project
val findBugsVersion: String by project
val midnightLibVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val fabricRecipeViewer: String by project
val aeVersion: String by project
val jeiVersion: String by project
val reiVersion: String by project
val githubUser: String by project
val githubRepo: String by project

plugins {
    id("fabric-loom") version "1.3.+"
    id("io.github.juuxel.loom-vineflower") version "1.11.0"
    id("com.github.gmazzo.buildconfig") version "4.0.4"
    java
}

base {
    version = "$minecraftVersion-$modVersion"
    group = modPackage
    archivesName.set("$modId-forge")
}

loom {
    mixin {
        defaultRefmapName.set("$modId.refmap.json")
    }

    if (project.findProperty("enableAccessWidener") == "true") {
        accessWidenerPath.set(file("src/main/resources/$modId.accesswidener"))
        println("Access widener enabled for project. Access widener path: ${loom.accessWidenerPath.get()}")
    }
}

repositories {
    maven("https://maven.parchmentmc.org/") // Parchment
    maven("https://api.modrinth.com/maven") // Midnight Lib
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

    // Project
    implementation("com.google.code.findbugs:jsr305:$findBugsVersion") // javax annotations
    modImplementation(include("maven.modrinth:midnightlib:$midnightLibVersion")!!) // config lib

    // Fabric
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion+$minecraftVersion")

    // Compile
    modCompileOnly("appeng:appliedenergistics2-fabric:$aeVersion")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:$reiVersion")

    // Runtime
    modLocalRuntime("appeng:appliedenergistics2-fabric:$aeVersion")
    when (fabricRecipeViewer) {
        "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion")
        "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion") { isTransitive = false }
        else -> throw GradleException("Invalid recipeViewer value: $fabricRecipeViewer")
    }
}

tasks {
    processResources {
        val resourceTargets = listOf("fabric.mod.json", "pack.mcmeta")

        val replaceProperties = mapOf(
            "license" to license,
            "minecraftVersion" to minecraftVersion,
            "version" to project.version as String,
            "modId" to modId,
            "modName" to modName,
            "modAuthor" to modAuthor,
            "modDescription" to modDescription,
            "fabricApiVersion" to fabricApiVersion,
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
