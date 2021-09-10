plugins {
    java
    id("org.jenkins-ci.jpi") version "0.43.0"
//    id("com.github.spotbugs") version "4.7.3"
}

group = "atos.net"
version = "0.5.0"

repositories {
    mavenCentral()
}

jenkinsPlugin {
    pluginFirstClassLoader = true

    coreVersion = "2.277.1"
    displayName = "RTC Jenkis Linker"                // Human-readable name of plugin.
    url = "http://wiki.jenkins-ci.org/display/JENKINS/SomePluginPage"   // URL for plugin on Jenkins wiki or elsewhere.
    gitHubUrl = "https://github.com/jenkinsci/some-plugin"              // Plugin URL on GitHub. Optional.
    shortName = "rjl"                                           // Plugin ID, defaults to the project name without trailing '-plugin'
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    implementation("org.jenkins-ci.plugins:credentials:2.1.10")
    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
}

tasks.register("version") {
    doLast {
        println(project.version)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
