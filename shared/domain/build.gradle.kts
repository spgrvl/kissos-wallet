plugins {
    id("kissos.feature")
    id("kissos.integration.testing")
    id("kissos.room")
}

android {
    namespace = "com.spgrvl.kissos.domain"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.data.core)

    implementation(libs.androidx.work)
    implementation(libs.datastore)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.opencsv)

    testImplementation(projects.shared.data.modelTesting)
    testImplementation(projects.shared.data.coreTesting)

    androidTestImplementation(libs.bundles.integration.testing)
    androidTestImplementation(libs.mockk.android)
}