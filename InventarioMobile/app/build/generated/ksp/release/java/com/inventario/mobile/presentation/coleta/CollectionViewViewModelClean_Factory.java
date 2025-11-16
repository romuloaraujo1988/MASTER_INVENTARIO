package com.inventario.mobile.presentation.coleta;

import com.inventario.mobile.data.migration.ColetaMigration;
import com.inventario.mobile.domain.usecase.BuscarColetasComFallbackUseCase;
import com.inventario.mobile.domain.usecase.BuscarColetasUseCase;
import com.inventario.mobile.domain.usecase.ObterUsuarioAtualUseCase;
import com.inventario.mobile.domain.usecase.RemoverColetaUseCase;
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
public final class CollectionViewViewModelClean_Factory implements Factory<CollectionViewViewModelClean> {
  private final Provider<BuscarColetasUseCase> buscarColetasUseCaseProvider;

  private final Provider<BuscarColetasComFallbackUseCase> buscarColetasComFallbackUseCaseProvider;

  private final Provider<ObterUsuarioAtualUseCase> obterUsuarioAtualUseCaseProvider;

  private final Provider<RemoverColetaUseCase> removerColetaUseCaseProvider;

  private final Provider<SincronizarColetasDoServidorUseCase> sincronizarColetasUseCaseProvider;

  private final Provider<ColetaMigration> coletaMigrationProvider;

  public CollectionViewViewModelClean_Factory(
      Provider<BuscarColetasUseCase> buscarColetasUseCaseProvider,
      Provider<BuscarColetasComFallbackUseCase> buscarColetasComFallbackUseCaseProvider,
      Provider<ObterUsuarioAtualUseCase> obterUsuarioAtualUseCaseProvider,
      Provider<RemoverColetaUseCase> removerColetaUseCaseProvider,
      Provider<SincronizarColetasDoServidorUseCase> sincronizarColetasUseCaseProvider,
      Provider<ColetaMigration> coletaMigrationProvider) {
    this.buscarColetasUseCaseProvider = buscarColetasUseCaseProvider;
    this.buscarColetasComFallbackUseCaseProvider = buscarColetasComFallbackUseCaseProvider;
    this.obterUsuarioAtualUseCaseProvider = obterUsuarioAtualUseCaseProvider;
    this.removerColetaUseCaseProvider = removerColetaUseCaseProvider;
    this.sincronizarColetasUseCaseProvider = sincronizarColetasUseCaseProvider;
    this.coletaMigrationProvider = coletaMigrationProvider;
  }

  @Override
  public CollectionViewViewModelClean get() {
    return newInstance(buscarColetasUseCaseProvider.get(), buscarColetasComFallbackUseCaseProvider.get(), obterUsuarioAtualUseCaseProvider.get(), removerColetaUseCaseProvider.get(), sincronizarColetasUseCaseProvider.get(), coletaMigrationProvider.get());
  }

  public static CollectionViewViewModelClean_Factory create(
      Provider<BuscarColetasUseCase> buscarColetasUseCaseProvider,
      Provider<BuscarColetasComFallbackUseCase> buscarColetasComFallbackUseCaseProvider,
      Provider<ObterUsuarioAtualUseCase> obterUsuarioAtualUseCaseProvider,
      Provider<RemoverColetaUseCase> removerColetaUseCaseProvider,
      Provider<SincronizarColetasDoServidorUseCase> sincronizarColetasUseCaseProvider,
      Provider<ColetaMigration> coletaMigrationProvider) {
    return new CollectionViewViewModelClean_Factory(buscarColetasUseCaseProvider, buscarColetasComFallbackUseCaseProvider, obterUsuarioAtualUseCaseProvider, removerColetaUseCaseProvider, sincronizarColetasUseCaseProvider, coletaMigrationProvider);
  }

  public static CollectionViewViewModelClean newInstance(BuscarColetasUseCase buscarColetasUseCase,
      BuscarColetasComFallbackUseCase buscarColetasComFallbackUseCase,
      ObterUsuarioAtualUseCase obterUsuarioAtualUseCase, RemoverColetaUseCase removerColetaUseCase,
      SincronizarColetasDoServidorUseCase sincronizarColetasUseCase,
      ColetaMigration coletaMigration) {
    return new CollectionViewViewModelClean(buscarColetasUseCase, buscarColetasComFallbackUseCase, obterUsuarioAtualUseCase, removerColetaUseCase, sincronizarColetasUseCase, coletaMigration);
  }
}
