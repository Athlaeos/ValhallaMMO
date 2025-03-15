import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    implementation(project(":v1_20_R4"))
}

tasks {
    reobfJar {
        paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
    }
    withType<JavaCompile>().configureEach {
        options.release = 21
    }
}
