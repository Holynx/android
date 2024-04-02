plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.applicationv3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.applicationv3"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.1.2")
    implementation("com.google.firebase:firebase-database-ktx:20.2.2")
    implementation("com.google.firebase:firebase-firestore-ktx:24.8.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.rengwuxian.materialedittext:library:2.1.4")

    //Calendar
    implementation ("com.jakewharton.threetenabp:threetenabp:1.3.1")

    //Implementation mySQL
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    //Implementation GoogleMap et autocompletion
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.libraries.places:places:3.4.0")
    //Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.2.3"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("com.google.firebase:firebase-firestore:24.11.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-functions:20.4.0")
    implementation("com.hendraanggrian.appcompat:socialview:0.1")
    //implementation("com.hendraanggrian.appcompat:socialview-commons:0.1")
    implementation("com.hendraanggrian.appcompat:socialview-autocomplete:0.1")

    //Gestion image
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.squareup.picasso:picasso:2.8")

    // badge notification
    implementation("com.nex3z:notification-badge:1.0.4")

    //Ajout Photo
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    implementation("androidx.cardview:cardview:1.0.0")

    //Calendar
    implementation("com.github.sundeepk:compact-calendar-view:3.0.0")
    implementation ("pub.devrel:easypermissions:3.0.0")
    implementation("com.applandeo:material-calendar-view:1.9.0")

    //Push notification
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))

    // Add the dependencies for the Firebase Cloud Messaging and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    //Transition
    implementation("androidx.core:core-ktx:1.7.0")

    //DatePIcker
    implementation("com.wdullaer:materialdatetimepicker:4.2.3")

    implementation("com.tbuonomo:dotsindicator:5.0")

    //Open Map
    implementation ("org.osmdroid:osmdroid-android:6.1.10")
}