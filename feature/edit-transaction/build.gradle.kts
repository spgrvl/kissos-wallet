plugins {
    id("kissos.feature")
}

android {
    namespace = "com.spgrvl.kissos.transaction"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.data.core)
    implementation(projects.shared.domain)
    implementation(projects.shared.ui.core)
    implementation(projects.shared.ui.navigation)
    implementation(projects.temp.legacyCode)
    implementation(projects.temp.oldDesign)
    implementation(projects.widget.balance)

    implementation(libs.google.code.scanner)
    implementation(libs.google.play.services.base)

    testImplementation(projects.shared.ui.testing)
}
