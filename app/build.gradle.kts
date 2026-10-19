plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "family.morrow"
    compileSdk = 35
    defaultConfig { applicationId = "family.morrow"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
    buildFeatures { compose = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    sourceSets["main"].assets.srcDir("${rootProject.layout.buildDirectory.get()}/content-assets")
}
val prepareContent by tasks.registering(Exec::class) {
    workingDir(rootDir)
    commandLine("python3", "scripts/prepare_content.py")
    inputs.files(fileTree("${rootDir}/content"), file("${rootDir}/scripts/prepare_content.py"))
    outputs.file("${rootProject.layout.buildDirectory.get()}/content-assets/content.yaml")
}
tasks.named("preBuild").configure { dependsOn(prepareContent) }
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("org.yaml:snakeyaml:2.4")
    testImplementation("junit:junit:4.13.2")
}
