import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

dependencies {
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    implementation(project(":v1_19_R1"))
}

tasks {
    reobfJar {
        paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
    }
    withType<JavaCompile>().configureEach {
        options.release = 21
    }
}
