package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.data.local.dao.ColetaDao;
import com.inventario.mobile.data.remote.api.ApiService;
import com.inventario.mobile.util.NetworkChecker;
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
public final class BuscarColetasComFallbackUseCase_Factory implements Factory<BuscarColetasComFallbackUseCase> {
  private final Provider<ApiService> apiServiceProvider;

  private final Provider<ColetaDao> coletaDaoProvider;

  private final Provider<NetworkChecker> networkCheckerProvider;

  public BuscarColetasComFallbackUseCase_Factory(Provider<ApiService> apiServiceProvider,
      Provider<ColetaDao> coletaDaoProvider, Provider<NetworkChecker> networkCheckerProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.coletaDaoProvider = coletaDaoProvider;
    this.networkCheckerProvider = networkCheckerProvider;
  }

  @Override
  public BuscarColetasComFallbackUseCase get() {
    return newInstance(apiServiceProvider.get(), coletaDaoProvider.get(), networkCheckerProvider.get());
  }

  public static BuscarColetasComFallbackUseCase_Factory create(
      Provider<ApiService> apiServiceProvider, Provider<ColetaDao> coletaDaoProvider,
      Provider<NetworkChecker> networkCheckerProvider) {
    return new BuscarColetasComFallbackUseCase_Factory(apiServiceProvider, coletaDaoProvider, networkCheckerProvider);
  }

  public static BuscarColetasComFallbackUseCase newInstance(ApiService apiService,
      ColetaDao coletaDao, NetworkChecker networkChecker) {
    return new BuscarColetasComFallbackUseCase(apiService, coletaDao, networkChecker);
  }
}
