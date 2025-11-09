package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.domain.repository.ColetaRepository;
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
public final class SincronizarColetasPendentesUseCase_Factory implements Factory<SincronizarColetasPendentesUseCase> {
  private final Provider<ColetaRepository> coletaRepositoryProvider;

  public SincronizarColetasPendentesUseCase_Factory(
      Provider<ColetaRepository> coletaRepositoryProvider) {
    this.coletaRepositoryProvider = coletaRepositoryProvider;
  }

  @Override
  public SincronizarColetasPendentesUseCase get() {
    return newInstance(coletaRepositoryProvider.get());
  }

  public static SincronizarColetasPendentesUseCase_Factory create(
      Provider<ColetaRepository> coletaRepositoryProvider) {
    return new SincronizarColetasPendentesUseCase_Factory(coletaRepositoryProvider);
  }

  public static SincronizarColetasPendentesUseCase newInstance(ColetaRepository coletaRepository) {
    return new SincronizarColetasPendentesUseCase(coletaRepository);
  }
}
