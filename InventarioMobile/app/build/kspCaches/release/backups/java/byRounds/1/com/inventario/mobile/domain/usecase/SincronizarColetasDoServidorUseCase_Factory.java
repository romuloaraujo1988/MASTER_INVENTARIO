package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.data.local.dao.ColetaDao;
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
public final class SincronizarColetasDoServidorUseCase_Factory implements Factory<SincronizarColetasDoServidorUseCase> {
  private final Provider<ApiService> apiServiceProvider;

  private final Provider<ColetaDao> coletaDaoProvider;

  public SincronizarColetasDoServidorUseCase_Factory(Provider<ApiService> apiServiceProvider,
      Provider<ColetaDao> coletaDaoProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.coletaDaoProvider = coletaDaoProvider;
  }

  @Override
  public SincronizarColetasDoServidorUseCase get() {
    return newInstance(apiServiceProvider.get(), coletaDaoProvider.get());
  }

  public static SincronizarColetasDoServidorUseCase_Factory create(
      Provider<ApiService> apiServiceProvider, Provider<ColetaDao> coletaDaoProvider) {
    return new SincronizarColetasDoServidorUseCase_Factory(apiServiceProvider, coletaDaoProvider);
  }

  public static SincronizarColetasDoServidorUseCase newInstance(ApiService apiService,
      ColetaDao coletaDao) {
    return new SincronizarColetasDoServidorUseCase(apiService, coletaDao);
  }
}
