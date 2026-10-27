plugins {
    id("kissos.feature")
    id("kissos.room")
}

android {
    namespace = "com.spgrvl.kissos.data.testing"
}

dependencies {
    implementation(projects.shared.data.core)
}
