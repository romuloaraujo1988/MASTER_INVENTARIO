# Tecnologias e Dependências - InventarioMobile

## 1. Versões e Compatibilidade

### 1.1 Android
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)
- **Compile SDK**: 34

### 1.2 Kotlin
- **Versão**: 1.9.10
- **Coroutines**: 1.7.3

### 1.3 Gradle
- **Gradle**: 8.2
- **Android Gradle Plugin**: 8.1.2

## 2. Dependências Principais

### 2.1 Android Jetpack
```gradle
// Core
implementation 'androidx.core:core-ktx:1.12.0'
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'androidx.activity:activity-ktx:1.8.2'
implementation 'androidx.fragment:fragment-ktx:1.6.2'

// Lifecycle
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'

// Navigation
implementation 'androidx.navigation:navigation-fragment-ktx:2.7.6'
implementation 'androidx.navigation:navigation-ui-ktx:2.7.6'
```

### 2.2 Material Design
```gradle
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
```### 2.3 Room Database
```gradle
def room_version = "2.6.1"
implementation "androidx.room:room-runtime:$room_version"
implementation "androidx.room:room-ktx:$room_version"
kapt "androidx.room:room-compiler:$room_version"
```

### 2.4 Retrofit e Networking
```gradle
def retrofit_version = "2.9.0"
implementation "com.squareup.retrofit2:retrofit:$retrofit_version"
implementation "com.squareup.retrofit2:converter-gson:$retrofit_version"
implementation "com.squareup.okhttp3:okhttp:4.12.0"
implementation "com.squareup.okhttp3:logging-interceptor:4.12.0"
```

### 2.5 Dependency Injection (Hilt)
```gradle
def hilt_version = "2.48"
implementation "com.google.dagger:hilt-android:$hilt_version"
kapt "com.google.dagger:hilt-compiler:$hilt_version"
implementation "androidx.hilt:hilt-work:1.1.0"
kapt "androidx.hilt:hilt-compiler:1.1.0"
```

### 2.6 WorkManager
```gradle
def work_version = "2.9.0"
implementation "androidx.work:work-runtime-ktx:$work_version"
```

### 2.7 QR Code Scanner
```gradle
implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
implementation 'com.google.zxing:core:3.5.2'
```

### 2.8 Image Loading
```gradle
implementation 'com.github.bumptech.glide:glide:4.16.0'
kapt 'com.github.bumptech.glide:compiler:4.16.0'
```### 2.9 Permissions
```gradle
implementation 'pub.devrel:easypermissions:3.0.0'
```

### 2.10 Security
```gradle
implementation 'androidx.security:security-crypto:1.1.0-alpha06'
implementation 'androidx.biometric:biometric:1.1.0'
```

### 2.11 Testing
```gradle
// Unit Testing
testImplementation 'junit:junit:4.13.2'
testImplementation 'org.mockito:mockito-core:5.7.0'
testImplementation 'org.mockito:mockito-inline:5.2.0'
testImplementation 'androidx.arch.core:core-testing:2.2.0'
testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3'

// Android Testing
androidTestImplementation 'androidx.test.ext:junit:1.1.5'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
androidTestImplementation 'androidx.test:runner:1.5.2'
androidTestImplementation 'androidx.test:rules:1.5.0'
androidTestImplementation "androidx.room:room-testing:2.6.1"
```

## 3. Configurações de Build

### 3.1 Build Features
```gradle
android {
    buildFeatures {
        viewBinding true
        dataBinding true
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = '1.8'
    }
}```

### 3.2 ProGuard/R8
```gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## 4. Plugins Necessários

### 4.1 Build.gradle (Project)
```gradle
plugins {
    id 'com.android.application' version '8.1.2' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.10' apply false
    id 'com.google.dagger.hilt.android' version '2.48' apply false
}
```

### 4.2 Build.gradle (App)
```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-kapt'
    id 'dagger.hilt.android.plugin'
    id 'kotlin-parcelize'
}
```

## 5. Configurações Específicas

### 5.1 Manifest Permissions
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
```