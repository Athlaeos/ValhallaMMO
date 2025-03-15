import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

dependencies {
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
    compileOnly(project(":v1_19_R1"))
}

tasks.reobfJar {
    paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
}
