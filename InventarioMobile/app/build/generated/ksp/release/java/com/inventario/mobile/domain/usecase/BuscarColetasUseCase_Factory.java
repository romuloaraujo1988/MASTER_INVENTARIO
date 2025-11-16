package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.data.remote.api.ApiService;
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

  public BuscarColetasUseCase_Factory(Provider<ApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public BuscarColetasUseCase get() {
    return newInstance(apiServiceProvider.get());
  }

  public static BuscarColetasUseCase_Factory create(Provider<ApiService> apiServiceProvider) {
    return new BuscarColetasUseCase_Factory(apiServiceProvider);
  }

  public static BuscarColetasUseCase newInstance(ApiService apiService) {
    return new BuscarColetasUseCase(apiService);
  }
}
