import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

// Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.mobdeve.s17.mobdeve.animoquest.project"
    compileSdk = 34
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.mobdeve.s17.mobdeve.animoquest.project"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "DIRECTIONS_API_KEY", "\"${localProperties.getProperty("DIRECTIONS_API_KEY")}\"")
        resValue("string", "MAPS_API_KEY", "\"${localProperties.getProperty("MAPS_API_KEY")}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-bom:31.2.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.libraries.places:places:2.6.0")
    implementation("com.google.maps.android:android-maps-utils:2.3.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-places:17.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.google.android.material:material:1.10.0")
}

apply(plugin = "com.google.gms.google-services")
