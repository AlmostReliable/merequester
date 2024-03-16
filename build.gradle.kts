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
    id("net.neoforged.gradle.userdev") version "7.0.97"
    id("com.github.gmazzo.buildconfig") version "4.0.4"
    java
}

base {
    version = "$minecraftVersion-$modVersion"
    group = modPackage
    archivesName.set("$modId-neoforge")
}

/**
 * Configures properties common to all run configurations
 */
val commonSystemProperties = mapOf(
        "forge.logging.console.level" to "debug",
        "fml.earlyprogresswindow" to "false",
        "appeng.tests" to "true",
)

runs {
    configureEach {
        workingDirectory = project.file("run")
        systemProperties = commonSystemProperties
        // property "mixin.debug.export", "true"
        modSource(sourceSets.main.get())
        jvmArguments("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
    }
    create("client") {
        systemProperties = commonSystemProperties + mapOf(
                "appeng.tests" to "true",
                "guideDev.ae2guide.sources" to file("guidebook").absolutePath,
        )
    }
    create("gametestWorld") {
        configure("client")
        programArguments("--username", "AE2Dev", "--quickPlaySingleplayer", "GametestWorld")
        systemProperties = mapOf(
                "appeng.tests" to "true",
                "guideDev.ae2guide.sources" to file("guidebook").absolutePath,
        )
    }
    create("guide") {
        configure("client")
        systemProperty("appeng.guide-dev.sources", file("guidebook").absolutePath)
        systemProperty("appeng.guide-dev.sources.namespace", modId)
        systemProperty("guideDev.ae2guide.startupPage", "$modId:getting-started.md")
    }
    create("server") {
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
    // Neoforge
    implementation("net.neoforged:neoforge:$neoVersion")

    // Compile
    compileOnly("appeng:appliedenergistics2-neoforge:$aeVersion")
    compileOnly("me.shedaniel:RoughlyEnoughItems-api:$reiVersion")

    // Runtime
    implementation("appeng:appliedenergistics2-neoforge:$aeVersion")
    when (forgeRecipeViewer) {
        "jei" -> implementation("mezz.jei:jei-$minecraftVersion-neoforge:$jeiVersion") { isTransitive = false }
        "rei" -> {
            implementation("me.shedaniel:RoughlyEnoughItems-neoforge:$reiVersion")
            implementation("dev.architectury:architectury-neoforge:11.1.17") // TODO: Remove on new REI version
        }

        "emi" -> implementation("dev.emi:emi-neoforge:$emiVersion+$minecraftVersion")
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
