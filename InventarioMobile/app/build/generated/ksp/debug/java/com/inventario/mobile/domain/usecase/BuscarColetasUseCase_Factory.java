package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.data.remote.api.ApiService;
import com.inventario.mobile.utils.PreferencesManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class BuscarColetasUseCase_Factory implements Factory<BuscarColetasUseCase> {
  private final Provider<ApiService> apiServiceProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public BuscarColetasUseCase_Factory(Provider<ApiService> apiServiceProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  @Override
  public BuscarColetasUseCase get() {
    return newInstance(apiServiceProvider.get(), preferencesManagerProvider.get());
  }

  public static BuscarColetasUseCase_Factory create(Provider<ApiService> apiServiceProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new BuscarColetasUseCase_Factory(apiServiceProvider, preferencesManagerProvider);
  }

  public static BuscarColetasUseCase newInstance(ApiService apiService,
      PreferencesManager preferencesManager) {
    return new BuscarColetasUseCase(apiService, preferencesManager);
  }
}
