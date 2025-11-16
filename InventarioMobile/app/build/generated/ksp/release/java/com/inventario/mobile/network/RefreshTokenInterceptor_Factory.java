package com.inventario.mobile.network;

import com.inventario.mobile.utils.PreferencesManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class RefreshTokenInterceptor_Factory implements Factory<RefreshTokenInterceptor> {
  private final Provider<PreferencesManager> preferencesManagerProvider;

  public RefreshTokenInterceptor_Factory(Provider<PreferencesManager> preferencesManagerProvider) {
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  @Override
  public RefreshTokenInterceptor get() {
    return newInstance(preferencesManagerProvider.get());
  }

  public static RefreshTokenInterceptor_Factory create(
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new RefreshTokenInterceptor_Factory(preferencesManagerProvider);
  }

  public static RefreshTokenInterceptor newInstance(PreferencesManager preferencesManager) {
    return new RefreshTokenInterceptor(preferencesManager);
  }
}
