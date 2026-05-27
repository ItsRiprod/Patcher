plugins {
    `maven-publish`
    id("hytale-mod") version "0.+"
}

group = "com.riprod"
version = "2.1.0"
val javaVersion = 25

repositories {
    mavenCentral()
    maven("https://maven.hytale-modding.info/releases") {
        name = "HytaleModdingReleases"
    }
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.jspecify)

    compileOnly(fileTree("lib/") { include("*.jar") })
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }

    withSourcesJar()
}

tasks.named<ProcessResources>("processResources") {
    val replaceProperties = mapOf(
        "plugin_group" to findProperty("plugin_group"),
        "plugin_maven_group" to project.group,
        "plugin_name" to project.name,
        "plugin_version" to project.version,
        "server_version" to findProperty("server_version"),
        "plugin_description" to findProperty("plugin_description"),
        "plugin_website" to findProperty("plugin_website"),
        "plugin_main_entrypoint" to findProperty("plugin_main_entrypoint"),
        "plugin_author" to findProperty("plugin_author")
    )

    filesMatching("manifest.json") {
        expand(replaceProperties)
    }

    inputs.properties(replaceProperties)
}

tasks.withType<Jar> {
    manifest {
        attributes["Specification-Title"] = rootProject.name
        attributes["Specification-Version"] = version
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] =
            providers.environmentVariable("COMMIT_SHA_SHORT")
                .map { "${version}-${it}" }
                .getOrElse(version.toString())
    }
}

publishing {
    repositories {
        // configure a maven repo here to publish (e.g. hytale-modding releases, jitpack, self-hosted)
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
