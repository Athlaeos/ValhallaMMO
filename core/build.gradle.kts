plugins {
    java
    `maven-publish`
}

dependencies {
    compileOnly("org.spigotmc", "spigot-api", "1.20.1-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")

    implementation("com.jeff-media:MorePersistentDataTypes:2.4.0")
    implementation("com.jeff-media:custom-block-data:2.2.2")

    compileOnly("me.lauriichan.spigot.justlootit:justlootit-core:1.7.0")
    compileOnly("me.lauriichan.spigot.justlootit:justlootit-nms-api:1.7.0") {
        exclude("me.lauriichan.laylib")
    }
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
    compileOnly("commons-io:commons-io:2.17.0")
    compileOnly("org.bstats:bstats-bukkit:3.0.3")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.8.5")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "${rootProject.group}"
            artifactId = "valhallammo-core"
            version = "${rootProject.version}"

            pom {
                name.set("ValhallaMMO")
                description.set(rootProject.description)
            }
        }
    }
}

