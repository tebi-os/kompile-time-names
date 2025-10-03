plugins {
    signing
}

version = BuildConfig.VERSION
group = BuildConfig.GROUP
description = BuildConfig.DESCRIPTION

signing {
    useInMemoryPgpKeys(
        properties["tebi.signing.key"].toString(),
        properties["tebi.signing.password"].toString(),
    )
}
