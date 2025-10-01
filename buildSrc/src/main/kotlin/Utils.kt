import org.gradle.api.publish.maven.MavenPom
import org.gradle.kotlin.dsl.assign


fun MavenPom.ktn(artifactId: String) {
    name = "${BuildConfig.GROUP}:$artifactId"
    description = BuildConfig.DESCRIPTION
    url = BuildConfig.URL_PRODUCT

    licenses {
        license {
            name = "The Apache License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
        }
    }

    developers {
        developer {
            name = "Desmond van der Meer"
            email = "desmond@tebi.com"
            organization = "Tebi"
            organizationUrl = BuildConfig.URL_TEBI
        }
    }

    scm {
        val conn = "scm:git:git" + BuildConfig.URL_GIT.removePrefix("https")
        connection = conn
        developerConnection = conn
        url = BuildConfig.URL_GIT.removeSuffix(".git") + "/tree/main"
    }
}
