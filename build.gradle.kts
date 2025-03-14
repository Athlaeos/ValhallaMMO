import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "me.athlaeos"
description = "A very big skills/leveling plugin with plenty of useful tools to make a unique experience"

//repos
allprojects {

    repositories {
        mavenCentral()
        maven {
            name = "jitpack.io"
            url = uri("https://jitpack.io")
        }
        maven {
            name = "spigot-repo"
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            name = "lauriichan-release"
            url = uri("https://maven.lauriichan.me/release")
        }
        maven {
            name = "jeff-media-public"
            url = uri("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")
        }
        maven {
            name = "enginehub"
            url = uri("https://maven.enginehub.org/repo/")
        }
        maven {
            name = "placeholderapi"
            url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        }
        maven {
            name = "codemc-repo"
            url = uri("https://repo.codemc.io/repository/maven-public/")
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.gradleup.shadow")

    ext {
        set("paperCompiler", ReobfArtifactConfiguration.REOBF_PRODUCTION)
    }

    dependencies {
        compileOnly("io.netty:netty-all:4.1.87.Final")

        //if we are an NMS module
        if (this@subprojects.name.startsWith("v1_")) {
            compileOnly(project(":core"))
            this@subprojects.tasks.assemble {
                dependsOn("reobfJar")
            }
        }
    }
}

//tasks
allprojects {
    tasks.build {
        dependsOn("shadowJar")
    }
}

tasks {
    runServer {
        version("1.21.4")
    }
}

dependencies {
    subprojects.forEach {
        implementation(it)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
