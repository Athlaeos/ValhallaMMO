import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "8.3.5"
}

java.disableAutoTargetJvm()

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

    //common dependencies
    dependencies {
        compileOnly("io.netty:netty-all:4.1.87.Final")
    }

    //if we are an NMS module
    if (this@subprojects.name.startsWith("v1_")) {
        //apply paper weight
        apply(plugin = "io.papermc.paperweight.userdev")

        //make sure it requires core
        dependencies.implementation(project(":core"))

        //make assemble require the re-obfuscated jar
        this@subprojects.tasks.assemble {
            dependsOn("reobfJar")
        }
    }

    tasks {
        withType<Javadoc>().configureEach {
            options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        }
        withType<JavaCompile>().configureEach {
            // Defaults to java 17, needs to be overridden in newer versions!
            options.release = 17
        }
    }
}

tasks {
    assemble {
        dependsOn("shadowJar")
    }
    runServer {
        version("1.21.4")
    }
}

dependencies {
    subprojects.forEach {
        //if its a version jar we want the reobf variant
        if (it.name.startsWith("v1_")) {
            runtimeOnly(project(":${it.name}", configuration = "reobf"))
        } else {
            implementation(it)
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
