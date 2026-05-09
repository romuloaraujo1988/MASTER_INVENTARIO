package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.data.local.dao.ColetaDao;
import com.inventario.mobile.data.remote.api.ColetaApi;
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
public final class EnviarColetasPendentesUseCase_Factory implements Factory<EnviarColetasPendentesUseCase> {
  private final Provider<ColetaDao> coletaDaoProvider;

  private final Provider<ColetaApi> coletaApiProvider;

  private final Provider<NetworkChecker> networkCheckerProvider;

  public EnviarColetasPendentesUseCase_Factory(Provider<ColetaDao> coletaDaoProvider,
      Provider<ColetaApi> coletaApiProvider, Provider<NetworkChecker> networkCheckerProvider) {
    this.coletaDaoProvider = coletaDaoProvider;
    this.coletaApiProvider = coletaApiProvider;
    this.networkCheckerProvider = networkCheckerProvider;
  }

  @Override
  public EnviarColetasPendentesUseCase get() {
    return newInstance(coletaDaoProvider.get(), coletaApiProvider.get(), networkCheckerProvider.get());
  }

  public static EnviarColetasPendentesUseCase_Factory create(Provider<ColetaDao> coletaDaoProvider,
      Provider<ColetaApi> coletaApiProvider, Provider<NetworkChecker> networkCheckerProvider) {
    return new EnviarColetasPendentesUseCase_Factory(coletaDaoProvider, coletaApiProvider, networkCheckerProvider);
  }

  public static EnviarColetasPendentesUseCase newInstance(ColetaDao coletaDao, ColetaApi coletaApi,
      NetworkChecker networkChecker) {
    return new EnviarColetasPendentesUseCase(coletaDao, coletaApi, networkChecker);
  }
}
