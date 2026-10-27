plugins {
    id("kissos.feature")
    id("kissos.room")
    id("kissos.integration.testing")
}

android {
    namespace = "com.spgrvl.kissos.data"
}

dependencies {
    implementation(projects.shared.base)
    api(projects.shared.data.model)

    api(libs.datastore)
    implementation(libs.bundles.ktor)

    testImplementation(projects.shared.data.modelTesting)
    androidTestImplementation(libs.bundles.integration.testing)
}
