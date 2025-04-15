plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "ValhallaMMO"

//Core Project
include("core")

//paper / folia support
include("platform_api", "platform_folia")

//NMS - Auto imports any new versions
File("$rootDir").listFiles()?.filter {
    //check the file is a directory, it starts with the version notation and contains a build file
    it.isDirectory && it.name.startsWith("v1_") && File("${it.path}/build.gradle.kts").exists()
}?.forEach {
    include(it.name)
}
