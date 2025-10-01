# Releasing

## Requirements

A GPG key pair is required to sign the artifacts to be published. Make sure that they public key is uploaded to
a key server that Maven Central supports.
For instructions see here: https://central.sonatype.org/publish/requirements/gpg/

The following gradle properties must be set, either directly in properties files, as command line arguments
using `-Dxxx=yyy` or as environment variables by prepending them with `ORG_GRADLE_PROJECT_`:

```properties
# Maven signing
signingInMemoryKey=<gpg-private-key-hardened-ascii>
signingInMemoryKeyId=<gpg-private-key-id>
signingInMemoryKeyPassword=<gpg-private-key-password>

# Maven Central publishing
mavenCentralUsername=<maven-central-user-token-name>
mavenCentralPassword=<maven-central-user-token-password>

# Gradle Plugin publishing
gradle.publish.key=<gradle-portal-publishing-key>
gradle.publish.secret=<gradle-portal-publishing-secret>
```

## Gradle tasks

These are the gradle tasks for publishing the different artifacts.

| Artifact        | Task name                                |
|-----------------|------------------------------------------|
| API             | `:api:publishToMavenCentral`             |
| Compiler plugin | `:compiler-plugin:publishToMavenCentral` |
| Gradle plugin   | `:gradle-plugin:publishPlugins`          |
| _All artifacts_ | `:publishKtn`                            |
