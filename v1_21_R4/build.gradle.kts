import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

dependencies {
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
    implementation(project(":v1_20_R4"))
    implementation(project(":v1_21_R1"))
    implementation(project(":v1_21_R2"))
}

tasks {
    reobfJar {
        paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
    }
    withType<JavaCompile>().configureEach {
        options.release = 21
    }
}
