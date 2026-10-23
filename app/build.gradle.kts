import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
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
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val cloudName = localProperties.getProperty("CLOUDINARY_CLOUD_NAME")?.trim()?.removeSurrounding("\"")
        val apiKey = localProperties.getProperty("CLOUDINARY_API_KEY")?.trim()?.removeSurrounding("\"")
        val apiSecret = localProperties.getProperty("CLOUDINARY_API_SECRET")?.trim()?.removeSurrounding("\"")
        val urlPg = localProperties.getProperty("PG_API_URL")?.trim()?.removeSurrounding("\"")
        val urlAuth = localProperties.getProperty("AUTH_API_URL")?.trim()?.removeSurrounding("\"")
        val urlFcm = localProperties.getProperty("FCM_API_BASE_URL")?.trim()?.removeSurrounding("\"")
        val urlMongo = localProperties.getProperty("MONGO_API_URL")?.trim()?.removeSurrounding("\"")
        val urlChatbot = localProperties.getProperty("CHATBOT_API_URL")?.trim()?.removeSurrounding("\"")
        val mapsKey = localProperties.getProperty("MAPS_API_KEY")?.trim()?.removeSurrounding("\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey.orEmpty()

        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudName\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"$apiKey\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"$apiSecret\"")

        buildConfigField("String", "API_BASE_URL_PG", "\"$urlPg\"")
        buildConfigField("String", "API_BASE_URL_AUTH", "\"$urlAuth\"")
        buildConfigField("String", "API_BASE_URL_MONGO", "\"$urlMongo\"")
        buildConfigField("String", "CHATBOT_API_URL", "\"$urlChatbot\"")
        buildConfigField("String", "FCM_API_BASE_URL", "\"$urlFcm\"")
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
    implementation("com.google.android.gms:play-services-maps:20.1.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // AndroidX e Material
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)

    // Credenciais e Login com Google
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Firebase (Usando a versão unificada via BoM)
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-installations")

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
