plugins {
    id("kissos.feature")
}

android {
    namespace = "com.spgrvl.kissos.ui"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.domain)
}