plugins {
    id("java")
    id("idea")
    id("signing")
    id("maven-publish")
    id("com.tddworks.sonatype-portal-publisher") version "0.0.1"
}

val ossrhUsername: String by project
val ossrhPassword: String by project

val version: String by project
val group: String by project
val artifact: String by project

project.group = group
project.version = version

repositories {
    mavenCentral()
    // paper-api
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    // paper api
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")


    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("net.kyori:adventure-api:4.21.0")
    testImplementation("net.kyori:adventure-text-minimessage:4.21.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}
sourceSets {
    main {
        java {
            srcDir("src")
        }
        resources {
            srcDir("resources")
        }
    }
    test {
        java {
            srcDir("test")
        }
    }
}
idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}


tasks.register<Copy>("prepareServer") {
    dependsOn("build")
    from(tasks.jar.get().archiveFile.get().asFile.path)
    rename(tasks.jar.get().archiveFile.get().asFile.name, "${project.name}.jar")
    into("G:\\paper\\plugins")
}

tasks {
    compileJava {
        options.compilerArgs.add("-parameters")
        options.encoding = "UTF-8"
    }
    compileTestJava { options.encoding = "UTF-8" }
    javadoc { options.encoding = "UTF-8" }
    test {
        useJUnitPlatform()

        maxHeapSize = "1G"

        testLogging {
            events("passed")
        }
    }
}
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                name.set(project.name)
                groupId = group
                artifactId = artifact
                version = version
                description.set("A simple Command library for PaperMC")
                url.set("https://github.com/Hugo5000/PaperMC-ConfigLib")
                licenses {
                    license {
                        name.set("GNU General Public License version 3")
                        url.set("https://opensource.org/license/gpl-3-0/")
                    }
                }
                developers {
                    developer {
                        name.set("Hugo")
                        email.set("noreply@hugob.at")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Hugo5000/PaperMC-ConfigLib.git")
                    developerConnection.set("scm:git:ssh://github.com/Hugo5000/PaperMC-ConfigLib.git")
                    url.set("http://github.com/Hugo5000/PaperMC-ConfigLib/tree/master")
                }
            }
            from(components["java"])
        }
    }
//    repositories {
//        maven {
//            name = "ossrh-staging-api"
//            url = uri("https://central.sonatype.com/api/v1/publish")
//            credentials {
//                username = ossrhUsername
//                password = ossrhPassword
//            }
//        }
//    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

sonatypePortalPublisher {
    authentication {
        username = ossrhUsername
        password = ossrhPassword
    }
    settings {
        autoPublish = false
    }
}