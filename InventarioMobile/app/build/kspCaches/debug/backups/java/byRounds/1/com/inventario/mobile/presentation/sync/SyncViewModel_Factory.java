package com.inventario.mobile.presentation.sync;

import com.inventario.mobile.data.migration.ColetaMigration;
import com.inventario.mobile.domain.usecase.EnviarColetasPendentesUseCase;
import com.inventario.mobile.domain.usecase.SincronizarColetasDoServidorUseCase;
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
public final class SyncViewModel_Factory implements Factory<SyncViewModel> {
  private final Provider<SincronizarColetasDoServidorUseCase> sincronizarColetasUseCaseProvider;

  private final Provider<EnviarColetasPendentesUseCase> enviarColetasPendentesUseCaseProvider;

  private final Provider<ColetaMigration> coletaMigrationProvider;

  public SyncViewModel_Factory(
      Provider<SincronizarColetasDoServidorUseCase> sincronizarColetasUseCaseProvider,
      Provider<EnviarColetasPendentesUseCase> enviarColetasPendentesUseCaseProvider,
      Provider<ColetaMigration> coletaMigrationProvider) {
    this.sincronizarColetasUseCaseProvider = sincronizarColetasUseCaseProvider;
    this.enviarColetasPendentesUseCaseProvider = enviarColetasPendentesUseCaseProvider;
    this.coletaMigrationProvider = coletaMigrationProvider;
  }

  @Override
  public SyncViewModel get() {
    return newInstance(sincronizarColetasUseCaseProvider.get(), enviarColetasPendentesUseCaseProvider.get(), coletaMigrationProvider.get());
  }

  public static SyncViewModel_Factory create(
      Provider<SincronizarColetasDoServidorUseCase> sincronizarColetasUseCaseProvider,
      Provider<EnviarColetasPendentesUseCase> enviarColetasPendentesUseCaseProvider,
      Provider<ColetaMigration> coletaMigrationProvider) {
    return new SyncViewModel_Factory(sincronizarColetasUseCaseProvider, enviarColetasPendentesUseCaseProvider, coletaMigrationProvider);
  }

  public static SyncViewModel newInstance(
      SincronizarColetasDoServidorUseCase sincronizarColetasUseCase,
      EnviarColetasPendentesUseCase enviarColetasPendentesUseCase,
      ColetaMigration coletaMigration) {
    return new SyncViewModel(sincronizarColetasUseCase, enviarColetasPendentesUseCase, coletaMigration);
  }
}
