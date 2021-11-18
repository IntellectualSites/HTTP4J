import org.cadixdev.gradle.licenser.LicenseExtension
import java.net.URI

plugins {
    java
    `maven-publish`
    signing

    id("org.cadixdev.licenser") version "0.6.1"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"

    idea
    eclipse
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:22.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.mock-server:mockserver-netty:5.11.2")
    testImplementation("org.mock-server:mockserver-client-java:5.11.2")
    testImplementation("ch.qos.logback:logback-classic:1.3.0-alpha4")
    compileOnly("com.google.code.gson:gson:2.8.9")
    testCompileOnly("com.google.code.gson:gson:2.8.9")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
}

tasks.compileJava.configure {
    options.release.set(8)
}

configurations.all {
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 16)
}

group = "com.intellectualsites.http"
version = "1.4-SNAPSHOT"

java {
    withSourcesJar()
    withJavadocJar()
}

configure<LicenseExtension> {
    header(rootProject.file("LICENSE"))
    include("**/*.java")
    newLine.set(false)
}

tasks {
    compileJava {
        options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "1000"))
        options.compilerArgs.add("-Xlint:all")
        for (disabledLint in arrayOf("processing", "path", "fallthrough", "serial"))
            options.compilerArgs.add("-Xlint:$disabledLint")
        options.isDeprecation = true
        options.encoding = "UTF-8"
    }

    javadoc {
        title = project.name + " " + project.version
        val opt = options as StandardJavadocDocletOptions
        opt.addStringOption("Xdoclint:none", "-quiet")
        opt.tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
        opt.links("https://javadoc.io/doc/org.jetbrains/annotations/22.0.0/")
        opt.links("https://www.javadoc.io/doc/com.google.code.gson/gson/2.8.8/")
    }
}

signing {
    if (!version.toString().endsWith("-SNAPSHOT")) {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        signing.isRequired
        sign(publishing.publications)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {

                name.set(project.name + " " + project.version)
                description.set("A simple, lightweight and tiny wrapper for Java's HttpURLConnection")
                url.set("https://github.com/Incendo/HTTP4J/")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("Citymonstret")
                        name.set("Alexander Söderberg")
                    }
                }

                scm {
                    url.set("https://github.com/Incendo/HTTP4J/")
                    connection.set("scm:https://Incendo@github.com/Incendo/HTTP4J.git")
                    developerConnection.set("scm:git://github.com/Incendo/HTTP4J.git")
                }

                issueManagement{
                    system.set("GitHub")
                    url.set("https://github.com/Incendo/HTTP4J/issues")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(URI.create("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(URI.create("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
