plugins {
    id("kissos.feature")
}

android {
    namespace = "com.spgrvl.kissos.attributions"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.domain)
    implementation(projects.shared.ui.core)
    implementation(projects.shared.ui.navigation)

    testImplementation(projects.shared.ui.testing)
}
