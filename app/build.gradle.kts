plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "br.com.acta"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "br.com.acta"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Sintaxe nativa do Gradle baseada em Providers integrada perfeitamente no Kotlin DSL
        val cloudName = providers.gradleProperty("CLOUDINARY_CLOUD_NAME").getOrElse("")
        val apiKey = providers.gradleProperty("CLOUDINARY_API_KEY").getOrElse("")
        val apiSecret = providers.gradleProperty("CLOUDINARY_API_SECRET").getOrElse("")

        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", cloudName)
        buildConfigField("String", "CLOUDINARY_API_KEY", apiKey)
        buildConfigField("String", "CLOUDINARY_API_SECRET", apiSecret)
    }

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // AndroidX e Material
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Credenciais e Login com Google
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Firebase (Usando a versão unificada via BoM)
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor ("org.projectlombok:lombok:1.18.32")
    implementation ("com.google.code.gson:gson:2.10.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Conversor Gson (para converter JSON automaticamente em Objetos)
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp3
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Interceptor para Log das requisições (opcional, mas muito útil para debug)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("com.cloudinary:cloudinary-android:3.1.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Jetpack Navigation Component
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
}