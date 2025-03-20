plugins {
    `maven-publish`
}

dependencies {
    compileOnly("org.spigotmc", "spigot-api", "1.20.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "${rootProject.group}"
            artifactId = "valhallammo-platform-api"
            version = "${rootProject.version}"

            pom {
                name.set("ValhallaMMO Platform API")
                description.set("A API used to support multiple platform")
            }
        }
    }
}
