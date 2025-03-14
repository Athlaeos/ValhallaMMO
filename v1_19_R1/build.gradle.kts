import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("1.19-R0.1-SNAPSHOT")
}

tasks.reobfJar {
    paperweight.reobfArtifactConfiguration =
        ext.get("paperCompiler") as? ReobfArtifactConfiguration ?: ReobfArtifactConfiguration.REOBF_PRODUCTION
}
