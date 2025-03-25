plugins {
    id("com.rtm516.mcxboxbroadcast.java-conventions")
}

dependencies {
    api(libs.gson)
    api(libs.bundles.jackson)
    api(libs.nimbus.jose.jwt)
    api(libs.java.websocket)
    api(libs.methanol)
    api(libs.minecraftauth)
    api(libs.bundles.protocol)

    api(libs.ice4j)
    api(libs.bundles.bouncycastle)
    api(libs.sctp4j)

    // api(libs.bundles.geyser)
    api(files("G:/storage/projects/files/Geyser-Spigot.jar"))
}

sourceSets {
    main {
        blossom {
            val info = GitInfo(indraGit)
            javaSources {
                property("version", info.version)
                property("gitVersion", info.gitVersion)
                property("buildNumber", info.buildNumber.toString())
                property("branch", info.branch)
                property("commit", info.commit)
                property("repository", info.repository)
            }
        }
    }
}

description = "core"
