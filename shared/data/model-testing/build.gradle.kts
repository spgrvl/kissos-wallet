plugins {
    id("ivy.feature")
}

android {
    namespace = "com.spgrvl.kissos.data.model.testing"
}

dependencies {
    implementation(projects.shared.data.model)

    implementation(libs.bundles.testing)
}