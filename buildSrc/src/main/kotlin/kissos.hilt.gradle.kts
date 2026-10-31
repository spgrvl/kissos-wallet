plugins {
    id("kissos.kotlin-android")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets.all {
        kotlin.srcDir("build/generated/ksp/$name/kotlin")
    }
}

dependencies {
    implementation(libs.bundles.hilt)
    ksp(catalog.library("hilt-compiler"))
    // Generates the bindings HiltWorkerFactory needs for @HiltWorker classes
    ksp(catalog.library("hilt-work-compiler"))
}
