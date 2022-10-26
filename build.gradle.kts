@file:Suppress("SpellCheckingInspection")
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    id("fabric-loom") version "1.0-SNAPSHOT"
    id("io.github.juuxel.loom-quiltflower") version "1.7.4"
    id("org.quiltmc.quilt-mappings-on-loom") version "4.2.1"

    id("com.modrinth.minotaur") version "2.4.4"
    id("com.github.breadmoirai.github-release") version "2.4.1"
}

group = "dev.nyon"
version = "1.3.1"
val authors = listOf("btwonion")
val githubRepo = "btwonion/SimpleAutoDrop"

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com")
}

dependencies {
    minecraft("com.mojang:minecraft:22w43a")
    mappings(loom.layered {
        //addLayer(quiltMappings.mappings("org.quiltmc:quilt-mappings:22w43a+build.4:v2"))
        officialMojangMappings()
    })
    modImplementation("net.fabricmc:fabric-loader:0.14.10")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.65.1+1.19.3")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.8.5+kotlin.1.7.20")

    modApi("com.terraformersmc:modmenu:4.0.6")
}

tasks {
    processResources {
        val modId = "autodrop"
        val modName = "SimpleAutoDrop"
        val modDescription = "Mod to automatically drop items from your inventory"

        inputs.property("id", modId)
        inputs.property("group", project.group)
        inputs.property("name", modName)
        inputs.property("description", modDescription)
        inputs.property("version", project.version)
        inputs.property("github", githubRepo)

        filesMatching("fabric.mod.json") {
            expand(
                "id" to modId,
                "group" to project.group,
                "name" to modName,
                "description" to modDescription,
                "version" to project.version,
                "github" to githubRepo,
            )
        }
    }

    register("releaseMod") {
        group = "mod"

        dependsOn("modrinth")
        dependsOn("modrinthSyncBody")
        dependsOn("githubRelease")
    }
}
val changelogText =
    file("changelogs/${project.version}.md").takeIf { it.exists() }?.readText() ?: "No changelog provided."

modrinth {
    token.set(findProperty("modrinth.token")?.toString())
    projectId.set("lg17V3i3")
    versionNumber.set("${project.version}")
    versionType.set("release")
    uploadFile.set(tasks["remapJar"])
    gameVersions.set(listOf("22w43a"))
    loaders.set(listOf("fabric", "quilt"))
    dependencies {
        required.project("fabric-api")
        required.project("fabric-language-kotlin")
    }
    changelog.set(changelogText)
    syncBodyFrom.set(file("README.md").readText())
}

githubRelease {
    token(findProperty("github.token")?.toString())

    val split = githubRepo.split("/")
    owner(split[0])
    repo(split[1])
    tagName("v${project.version}")
    body(changelogText)
    releaseAssets(tasks["remapJar"].outputs.files)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs += "-Xskip-prerelease-check"
}