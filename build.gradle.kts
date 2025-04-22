import com.diffplug.gradle.spotless.SpotlessPlugin
import com.vanniktech.maven.publish.SonatypeHost
import java.net.URI

plugins {
    java
    signing

    alias(libs.plugins.spotless)
    alias(libs.plugins.publish)

    idea
    eclipse
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.gson)
    implementation(libs.annotations)

    testImplementation(libs.jupiter)
    testImplementation(libs.mockserverNetty)
    testImplementation(libs.mockserverClient)
    testImplementation(libs.logback)
    testCompileOnly(libs.gson)
    testRuntimeOnly(libs.junitPlatform)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
}

tasks.compileJava.configure {
    options.release.set(8)
}

group = "com.intellectualsites.http"
version = "1.8"

spotless {
    java {
        licenseHeaderFile(rootProject.file("LICENSE"))
        target("**/*.java")
    }
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
        title = project.name
        val opt = options as StandardJavadocDocletOptions
        opt.addStringOption("Xdoclint:none", "-quiet")
        opt.tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
        opt.links("https://javadoc.io/doc/org.jetbrains/annotations/" + libs.annotations.get().versionConstraint.toString())
        opt.links("https://javadoc.io/doc/com.google.code.gson/gson/" + libs.gson.get().versionConstraint.toString())
        opt.noTimestamp()
    }

    withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

signing {
    if (!project.hasProperty("skip.signing") && !version.toString().endsWith("-SNAPSHOT")) {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        signing.isRequired
        sign(publishing.publications)
    }
}

mavenPublishing {
    coordinates(
        groupId = "$group",
        artifactId = project.name,
        version = "${project.version}",
    )

    pom {

        name.set(project.name + " " + project.version)
        description.set("A simple, lightweight and tiny wrapper for Java's HttpURLConnection")
        url.set("https://github.com/IntellectualSites/HTTP4J/")

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
                organization.set("IntellectualSites")
                organizationUrl.set("https://github.com/IntellectualSites/")
            }
            developer {
                id.set("NotMyFault")
                name.set("Alexander Brandes")
                organization.set("IntellectualSites")
                organizationUrl.set("https://github.com/IntellectualSites")
                email.set("contact(at)notmyfault.dev")
            }
        }

        scm {
            url.set("https://github.com/IntellectualSites/HTTP4J/")
            connection.set("scm:git:https://github.com/IntellectualSites/HTTP4J.git")
            developerConnection.set("scm:git:git@github.com:IntellectualSites/HTTP4J.git")
            tag.set("${project.version}")
        }

        issueManagement{
            system.set("GitHub")
            url.set("https://github.com/IntellectualSites/HTTP4J/issues")
        }

        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    }
}
