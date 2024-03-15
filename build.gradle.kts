@file:Suppress("UnstableApiUsage")

val license: String by project
val minecraftVersion: String by project
val modVersion: String by project
val modPackage: String by project
val modId: String by project
val modName: String by project
val modAuthor: String by project
val modDescription: String by project
val parchmentVersion: String by project
val neoVersion: String by project
val forgeRecipeViewer: String by project
val aeVersion: String by project
val wtlibVersion: String by project
val jeiVersion: String by project
val reiVersion: String by project
val emiVersion: String by project
val githubUser: String by project
val githubRepo: String by project

plugins {
    id("dev.architectury.loom") version "1.4.+"
    id("com.github.gmazzo.buildconfig") version "4.0.4"
    java
}

base {
    version = "$minecraftVersion-$modVersion"
    group = modPackage
    archivesName.set("$modId-neoforge")
}

loom {
    silentMojangMappingsLicense()

    runs {
        create("guide") {
            client()
            property("appeng.guide-dev.sources", file("guidebook").absolutePath)
            property("appeng.guide-dev.sources.namespace", modId)
            property("guideDev.ae2guide.startupPage", "$modId:getting-started.md")
        }

        forEach {
            it.vmArgs("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
        }
    }
}

repositories {
    maven("https://maven.neoforged.net/releases") // Neoforge
    maven("https://maven.parchmentmc.org") // Parchment
    maven("https://modmaven.dev") // Applied Energistics 2
    maven("https://maven.blamejared.com") // JEI
    maven("https://maven.shedaniel.me") // REI
    maven("https://maven.terraformersmc.com") // EMI
    mavenLocal()
}

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
    })

    // Neoforge
    neoForge("net.neoforged:neoforge:$neoVersion")

    // Compile
    modCompileOnly("appeng:appliedenergistics2-neoforge:$aeVersion")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api:$reiVersion")

    // Runtime
    modLocalRuntime("appeng:appliedenergistics2-neoforge:$aeVersion")
    when (forgeRecipeViewer) {
        "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-neoforge:$jeiVersion") { isTransitive = false }
        "rei" -> {
            modLocalRuntime("me.shedaniel:RoughlyEnoughItems-neoforge:$reiVersion")
            modLocalRuntime("dev.architectury:architectury-neoforge:11.1.17") // TODO: Remove on new REI version
        }

        "emi" -> modLocalRuntime("dev.emi:emi-neoforge:$emiVersion+$minecraftVersion")
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
                "neoVersion" to neoVersion,
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
