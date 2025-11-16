package com.inventario.mobile.presentation.sync;

import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase;
import com.inventario.mobile.domain.usecase.SincronizarDadosUseCase;
import com.inventario.mobile.sync.SyncManager;
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
  private final Provider<SincronizarDadosUseCase> sincronizarDadosUseCaseProvider;

  private final Provider<SincronizarColetasPendentesUseCase> sincronizarColetasPendentesUseCaseProvider;

  private final Provider<SyncManager> syncManagerProvider;

  public SyncViewModel_Factory(Provider<SincronizarDadosUseCase> sincronizarDadosUseCaseProvider,
      Provider<SincronizarColetasPendentesUseCase> sincronizarColetasPendentesUseCaseProvider,
      Provider<SyncManager> syncManagerProvider) {
    this.sincronizarDadosUseCaseProvider = sincronizarDadosUseCaseProvider;
    this.sincronizarColetasPendentesUseCaseProvider = sincronizarColetasPendentesUseCaseProvider;
    this.syncManagerProvider = syncManagerProvider;
  }

  @Override
  public SyncViewModel get() {
    return newInstance(sincronizarDadosUseCaseProvider.get(), sincronizarColetasPendentesUseCaseProvider.get(), syncManagerProvider.get());
  }

  public static SyncViewModel_Factory create(
      Provider<SincronizarDadosUseCase> sincronizarDadosUseCaseProvider,
      Provider<SincronizarColetasPendentesUseCase> sincronizarColetasPendentesUseCaseProvider,
      Provider<SyncManager> syncManagerProvider) {
    return new SyncViewModel_Factory(sincronizarDadosUseCaseProvider, sincronizarColetasPendentesUseCaseProvider, syncManagerProvider);
  }

  public static SyncViewModel newInstance(SincronizarDadosUseCase sincronizarDadosUseCase,
      SincronizarColetasPendentesUseCase sincronizarColetasPendentesUseCase,
      SyncManager syncManager) {
    return new SyncViewModel(sincronizarDadosUseCase, sincronizarColetasPendentesUseCase, syncManager);
  }
}
