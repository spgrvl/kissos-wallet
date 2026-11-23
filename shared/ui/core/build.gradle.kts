plugins {
    id("kissos.feature")
    id("kissos.paparazzi")
}

android {
    namespace = "com.spgrvl.kissos.ui"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.domain)
    implementation(libs.bundles.activity)
}