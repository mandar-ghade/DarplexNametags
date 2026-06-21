import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.4.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.pvphub.me/tofaa")
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
//    flatDir {
//        dirs("libs")
//    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")
    implementation("io.github.tofaa2:spigot:3.0.3-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.12.1")
//    compileOnly("io.github.alexdev03:unlimitednametags-api-paper:2.0.0")
//    compileOnly("com.google.code.gson:gson:2.11.0")

}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.named<ShadowJar>("shadowJar") {
    relocate("me.tofaa.entitylib", "com.darplex.darplexNametags.libraries.entitylib")
}
tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.1")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}
