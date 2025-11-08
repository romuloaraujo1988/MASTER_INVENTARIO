# Script para gerar APK com máxima compatibilidade
Write-Host "Gerando APK com configuracoes de maxima compatibilidade..." -ForegroundColor Green

# Backup do build.gradle atual
Copy-Item ".\app\build.gradle" ".\app\build.gradle.backup" -Force

# Criar versão simplificada do build.gradle
$buildGradleContent = @"
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-parcelize'
}

android {
    namespace 'com.inventario.mobile'
    compileSdk 34

    defaultConfig {
        applicationId "com.inventario.mobile"
        minSdk 21
        targetSdk 33
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
        debug {
            debuggable true
            applicationIdSuffix ".debug"
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = '1.8'
    }
    
    buildFeatures {
        viewBinding true
        buildConfig true
    }
    
    packagingOptions {
        resources {
            excludes += '/META-INF/{AL2.0,LGPL2.1}'
            excludes += '/META-INF/DEPENDENCIES'
            excludes += '/META-INF/LICENSE*'
            excludes += '/META-INF/NOTICE*'
        }
    }
}

dependencies {
    // Core Android - versões estáveis
    implementation 'androidx.core:core-ktx:1.10.1'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.2'
    implementation 'androidx.activity:activity-ktx:1.7.2'
    implementation 'androidx.fragment:fragment-ktx:1.6.1'
    
    // Navigation
    implementation 'androidx.navigation:navigation-fragment-ktx:2.6.0'
    implementation 'androidx.navigation:navigation-ui-ktx:2.6.0'
    
    // ViewModel and LiveData
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.2'
    
    // SwipeRefreshLayout
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
    
    // Kotlin Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    
    // Room Database
    implementation 'androidx.room:room-runtime:2.5.2'
    implementation 'androidx.room:room-ktx:2.5.2'
    
    // Networking
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
    
    // QR Code Scanner
    implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
    implementation 'com.google.zxing:core:3.5.2'
    
    // Work Manager
    implementation 'androidx.work:work-runtime-ktx:2.8.1'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
"@

# Escrever novo build.gradle
$buildGradleContent | Out-File -FilePath ".\app\build.gradle" -Encoding UTF8 -Force

Write-Host "Build.gradle atualizado com configuracoes compatíveis" -ForegroundColor Cyan

# Limpar e construir
Write-Host "Limpando projeto..." -ForegroundColor Cyan
& .\gradlew clean --no-daemon

Write-Host "Gerando APK compatível..." -ForegroundColor Green
& .\gradlew assembleDebug --no-daemon

# Verificar resultado
$apkPath = ".\app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $apkInfo = Get-Item $apkPath
    Write-Host "APK compatível gerado com sucesso!" -ForegroundColor Green
    Write-Host "Localizacao: $($apkInfo.FullName)" -ForegroundColor White
    Write-Host "Tamanho: $([math]::Round($apkInfo.Length / 1MB, 2)) MB" -ForegroundColor White
    
    # Copiar APK para local de fácil acesso
    $destPath = ".\app-inventario-mobile-compativel.apk"
    Copy-Item $apkPath $destPath -Force
    Write-Host "APK copiado para: $destPath" -ForegroundColor Yellow
} else {
    Write-Host "Falha ao gerar APK compatível" -ForegroundColor Red
    # Restaurar backup
    Copy-Item ".\app\build.gradle.backup" ".\app\build.gradle" -Force
    Write-Host "Build.gradle original restaurado" -ForegroundColor Yellow
}

Write-Host "Script concluido!" -ForegroundColor Magenta