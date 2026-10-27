plugins {
    id("ivy.widget")
}

android {
    namespace = "com.spgrvl.kissos.widget"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.domain)
}
