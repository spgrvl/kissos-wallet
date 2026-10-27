plugins {
    id("ivy.feature")
    id("ivy.room")
}

android {
    namespace = "com.spgrvl.kissos.data.testing"
}

dependencies {
    implementation(projects.shared.data.core)
}
