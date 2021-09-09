import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    java
    `maven-publish`
    signing

    id("org.cadixdev.licenser") version "0.6.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:22.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("org.mock-server:mockserver-netty:5.11.0")
    testImplementation("org.mock-server:mockserver-client-java:5.11.0")
    testImplementation("ch.qos.logback:logback-classic:1.3.0-alpha4")
    compileOnly("com.google.code.gson:gson:2.8.8")
    testCompileOnly("com.google.code.gson:gson:2.8.8")
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
version = "1.4"
var versuffix by extra("SNAPSHOT")
version = if (!project.hasProperty("release")) {
    String.format("%s-%s", project.version, versuffix)
} else {
    String.format(project.version as String)
}

java {
    withSourcesJar()
    withJavadocJar()
}

configure<LicenseExtension> {
    header.set(resources.text.fromFile(file("LICENSE")))
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
        opt.addBooleanOption("html5", true)
        opt.links("https://javadoc.io/doc/org.jetbrains/annotations/22.0.0/")
        opt.links("https://www.javadoc.io/doc/com.google.code.gson/gson/2.8.8/")
    }
}

signing {
    if (!version.toString().endsWith("-SNAPSHOT")) {
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

    repositories {
        mavenLocal()
        val nexusUsername: String? by project
        val nexusPassword: String? by project
        if (nexusUsername != null && nexusPassword != null) {
            maven {
                val releasesRepositoryUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotRepositoryUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                url = uri(
                    if (version.toString().endsWith("-SNAPSHOT")) snapshotRepositoryUrl
                    else releasesRepositoryUrl
                )

                credentials {
                    username = nexusUsername
                    password = nexusPassword
                }
            }
        } else {
            logger.warn("No nexus repository is added; nexusUsername or nexusPassword is null.")
        }
    }
}
