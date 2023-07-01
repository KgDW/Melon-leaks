import net.minecraftforge.gradle.userdev.UserDevExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.spongepowered.asm.gradle.plugins.MixinExtension

group = "melon"
version = "4.4"

buildscript {
    repositories {
        mavenCentral()
        maven("https://files.minecraftforge.net/maven")
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }

    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:4.+")
        classpath("org.spongepowered:mixingradle:0.7.+")
    }
}

plugins {
    idea
    java
    kotlin("jvm")
}

apply {
    plugin("net.minecraftforge.gradle")
    plugin("org.spongepowered.mixin")
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://impactdevelopment.github.io/maven/")
    maven("https://jitpack.io")
}

val library: Configuration by configurations.creating

val kotlinxCoroutineVersion: String by project
val minecraftVersion: String by project
val forgeVersion: String by project
val mappingsChannel: String by project
val mappingsVersion: String by project

dependencies {
    // Jar packaging
    fun ModuleDependency.exclude(moduleName: String): ModuleDependency {
        return exclude(mapOf("module" to moduleName))
    }

    fun jarOnly(dependencyNotation: Any) {
        library(dependencyNotation)
    }

    fun implementationAndLibrary(dependencyNotation: String) {
        implementation(dependencyNotation)
        library(dependencyNotation)
    }

    fun implementationAndLibrary(
        dependencyNotation: String,
        dependencyConfiguration: ExternalModuleDependency.() -> Unit
    ) {
        implementation(dependencyNotation, dependencyConfiguration)
        library(dependencyNotation, dependencyConfiguration)
    }

    // Forge
    val minecraft = "minecraft"
    minecraft("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")

    // Dependencies
    implementationAndLibrary("org.jetbrains.kotlin:kotlin-stdlib")
    implementationAndLibrary("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementationAndLibrary("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")

    implementationAndLibrary("org.spongepowered:mixin:0.8-SNAPSHOT") {
        exclude("commons-io")
        exclude("gson")
        exclude("guava")
        exclude("launchwrapper")
        exclude("log4j-core")
    }

    annotationProcessor("org.spongepowered:mixin:0.8.2:processor") {
        exclude("gson")
    }

    implementationAndLibrary("org.joml:joml:1.10.4")
}

idea {
    module {
        excludeDirs.add(file("log.txt"))
        excludeDirs.add(file("run"))
    }
}

configure<MixinExtension> {
    defaultObfuscationEnv = "searge"
    add(sourceSets["main"], "mixins.melon.refmap.json")
}

configure<UserDevExtension> {

    setAccessTransformer(layout.projectDirectory.file("/src/main/resources/melon_at.cfg").asFile)

    mappings(mappingsChannel, mappingsVersion)

    runs {
        create("client") {
            workingDirectory = project.file("run").path
            ideaModule("${rootProject.name}.${project.name}.main")

            properties(
                mapOf(
                    "fml.coreMods.load" to "dev.zenhao.melon.mixin.MixinLoaderForge",
                    "mixin.env.disableRefMap" to "true",

                    "forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP",
                    "forge.logging.console.level" to "debug",

                    "FMLAT" to "melon_at.cfg"
                )
            )
        }
    }
}

tasks {
//    clean {
//        val set = mutableSetOf<Any>()
//        buildDir.listFiles()?.filterNotTo(set) {
//            it.name == "fg_cache"
//        }
//        delete = set
//    }

    compileJava {
        options.encoding = "UTF-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlin.contracts.ExperimentalContracts",
                "-Xlambdas=indy",
                "-Xcontext-receivers"
            )
        }
    }

    processResources {
        inputs.property("version", project.version)
        inputs.property("mcversion", "$minecraftVersion-$forgeVersion")

        exclude("**/rawimagefiles")

        from(sourceSets.main.get().resources.srcDirs) {
            include("mixins.melon.json")
            include("mcmod.info")
            expand(
                "version" to version,
                "mcversion" to "$minecraftVersion-$forgeVersion",
                "modVersion" to version
            )
            exclude("mcmod.info")
        }

        rename("(.+_at.cfg)", "META-INF/$1")
    }

    jar {
        manifest {
            attributes(
                "FMLCorePluginContainsFMLMod" to true,
                "FMLCorePlugin" to "dev.zenhao.melon.mixin.MixinLoaderForge",
                "MixinConfigs" to "mixins.melon.json",
                "tweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
                "TweakOrder" to 0,
                "ForceLoadAsMod" to true,
                "FMLAT" to "melon_at.cfg",
                "Manifest-Version" to 1.0,
                "Agent-Class" to "dev.zenhao.melon.inject.AgentMain"
            )
        }

        from(
            library.map {
                if (it.isDirectory) it
                else zipTree(it)
            }
        )
    }

    register<Copy>("AutoBuild") {
        val modPath = System.getProperty("user.home") + "/AppData/Roaming/.minecraft/mods/"

        group = "build"
        dependsOn("build")
        if (file("$modPath/Melon*.jar").exists()) {
            delete {
                fileTree("$modPath/").matching {
                    include("Melon*.jar")
                }
            }
        }
        from("build/libs/")
        include("Melon*.jar")
        into("$modPath/")
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}