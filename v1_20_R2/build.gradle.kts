import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("1.20.2-R0.1-SNAPSHOT")
    compileOnly(project(":v1_19_R1"))
}

tasks.reobfJar {
    paperweight.reobfArtifactConfiguration =
        ext.get("paperCompiler") as? ReobfArtifactConfiguration ?: ReobfArtifactConfiguration.REOBF_PRODUCTION
}
