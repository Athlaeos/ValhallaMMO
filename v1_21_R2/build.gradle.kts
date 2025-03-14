import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
    compileOnly(project(":v1_20_R4"))
    compileOnly(project(":v1_21_R1"))
}

tasks.reobfJar {
    paperweight.reobfArtifactConfiguration =
        ext.get("paperCompiler") as? ReobfArtifactConfiguration ?: ReobfArtifactConfiguration.REOBF_PRODUCTION
}
