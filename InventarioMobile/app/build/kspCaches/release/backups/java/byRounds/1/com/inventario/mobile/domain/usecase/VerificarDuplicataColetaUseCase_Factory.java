package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.data.remote.api.ColetaApi;
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
public final class VerificarDuplicataColetaUseCase_Factory implements Factory<VerificarDuplicataColetaUseCase> {
  private final Provider<ColetaApi> coletaApiProvider;

  public VerificarDuplicataColetaUseCase_Factory(Provider<ColetaApi> coletaApiProvider) {
    this.coletaApiProvider = coletaApiProvider;
  }

  @Override
  public VerificarDuplicataColetaUseCase get() {
    return newInstance(coletaApiProvider.get());
  }

  public static VerificarDuplicataColetaUseCase_Factory create(
      Provider<ColetaApi> coletaApiProvider) {
    return new VerificarDuplicataColetaUseCase_Factory(coletaApiProvider);
  }

  public static VerificarDuplicataColetaUseCase newInstance(ColetaApi coletaApi) {
    return new VerificarDuplicataColetaUseCase(coletaApi);
  }
}
