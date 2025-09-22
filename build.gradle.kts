plugins {
    id("java")
}

group = property("maven_group").toString()
version = property("version").toString()
val projectName = property("archives_base_name").toString()

tasks.jar {
    archiveBaseName = projectName
    manifest {
        attributes("Manifest-Version" to 1.0, "Main-Class" to "io.github.arkosammy12.jchip.Main")
    }
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("info.picocli:picocli:${property("picocli_version")}")
    implementation("com.google.code.gson:gson:${property("gson_version")}")
}

tasks.test {
    useJUnitPlatform()
}