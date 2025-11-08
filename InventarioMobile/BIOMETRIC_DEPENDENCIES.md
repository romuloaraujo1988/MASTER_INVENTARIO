# 🔐 Dependências para Autenticação Biométrica

## Adicionar ao build.gradle (app)

```gradle
dependencies {
    // ... outras dependências existentes ...
    
    // Autenticação Biométrica
    implementation "androidx.biometric:biometric:1.2.0-alpha05"
    
    // Armazenamento Seguro (Criptografia)
    implementation "androidx.security:security-crypto:1.1.0-alpha06"
    
    // Opcional: Para testes
    androidTestImplementation "androidx.biometric:biometric:1.2.0-alpha05"
}
```

## Permissões no AndroidManifest.xml

```xml
<!-- Já deve estar presente -->
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<uses-permission android:name="android.permission.USE_FINGERPRINT" />
```

## Versão Mínima do SDK

A autenticação biométrica requer:
- **minSdk**: 23 (Android 6.0) ou superior
- **targetSdk**: 33 ou superior (recomendado)

## Verificar no build.gradle (app)

```gradle
android {
    defaultConfig {
        minSdk 23  // Mínimo para biometria
        targetSdk 33
        // ...
    }
}
```

## Após Adicionar as Dependências

1. Sync do Gradle
2. Clean Project
3. Rebuild Project

## Comandos

```bash
# No diretório do projeto Android
cd InventarioMobile

# Sync e build
.\gradlew.bat clean build
```

## Verificação

Após adicionar as dependências, você pode verificar se foram instaladas corretamente:

```bash
.\gradlew.bat app:dependencies
```

Procure por:
- `androidx.biometric:biometric:1.2.0-alpha05`
- `androidx.security:security-crypto:1.1.0-alpha06`
