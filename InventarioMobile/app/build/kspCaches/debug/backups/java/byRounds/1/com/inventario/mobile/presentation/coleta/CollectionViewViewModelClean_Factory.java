package com.inventario.mobile.presentation.coleta;

import com.inventario.mobile.data.migration.ColetaMigration;
import com.inventario.mobile.domain.usecase.BaixarRelatorioFotoPdfUseCase;
import com.inventario.mobile.domain.usecase.BuscarColetasComFallbackUseCase;
import com.inventario.mobile.domain.usecase.BuscarColetasUseCase;
import com.inventario.mobile.domain.usecase.BuscarInfoRelatorioFotoUseCase;
import com.inventario.mobile.domain.usecase.ExcluirColetaPendenteUseCase;
import com.inventario.mobile.domain.usecase.ObterUsuarioAtualUseCase;
import com.inventario.mobile.domain.usecase.ReenviarColetaUseCase;
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

  private final Provider<ReenviarColetaUseCase> reenviarColetaUseCaseProvider;

  private final Provider<ExcluirColetaPendenteUseCase> excluirColetaPendenteUseCaseProvider;

  private final Provider<BuscarInfoRelatorioFotoUseCase> buscarInfoRelatorioFotoUseCaseProvider;

  private final Provider<BaixarRelatorioFotoPdfUseCase> baixarRelatorioFotoPdfUseCaseProvider;

  public CollectionViewViewModelClean_Factory(
      Provider<BuscarColetasUseCase> buscarColetasUseCaseProvider,
      Provider<BuscarColetasComFallbackUseCase> buscarColetasComFallbackUseCaseProvider,
      Provider<ObterUsuarioAtualUseCase> obterUsuarioAtualUseCaseProvider,
      Provider<RemoverColetaUseCase> removerColetaUseCaseProvider,
      Provider<SincronizarColetasDoServidorUseCase> sincronizarColetasUseCaseProvider,
      Provider<ColetaMigration> coletaMigrationProvider,
      Provider<ReenviarColetaUseCase> reenviarColetaUseCaseProvider,
      Provider<ExcluirColetaPendenteUseCase> excluirColetaPendenteUseCaseProvider,
      Provider<BuscarInfoRelatorioFotoUseCase> buscarInfoRelatorioFotoUseCaseProvider,
      Provider<BaixarRelatorioFotoPdfUseCase> baixarRelatorioFotoPdfUseCaseProvider) {
    this.buscarColetasUseCaseProvider = buscarColetasUseCaseProvider;
    this.buscarColetasComFallbackUseCaseProvider = buscarColetasComFallbackUseCaseProvider;
    this.obterUsuarioAtualUseCaseProvider = obterUsuarioAtualUseCaseProvider;
    this.removerColetaUseCaseProvider = removerColetaUseCaseProvider;
    this.sincronizarColetasUseCaseProvider = sincronizarColetasUseCaseProvider;
    this.coletaMigrationProvider = coletaMigrationProvider;
    this.reenviarColetaUseCaseProvider = reenviarColetaUseCaseProvider;
    this.excluirColetaPendenteUseCaseProvider = excluirColetaPendenteUseCaseProvider;
    this.buscarInfoRelatorioFotoUseCaseProvider = buscarInfoRelatorioFotoUseCaseProvider;
    this.baixarRelatorioFotoPdfUseCaseProvider = baixarRelatorioFotoPdfUseCaseProvider;
  }

  @Override
  public CollectionViewViewModelClean get() {
    return newInstance(buscarColetasUseCaseProvider.get(), buscarColetasComFallbackUseCaseProvider.get(), obterUsuarioAtualUseCaseProvider.get(), removerColetaUseCaseProvider.get(), sincronizarColetasUseCaseProvider.get(), coletaMigrationProvider.get(), reenviarColetaUseCaseProvider.get(), excluirColetaPendenteUseCaseProvider.get(), buscarInfoRelatorioFotoUseCaseProvider.get(), baixarRelatorioFotoPdfUseCaseProvider.get());
  }

  public static CollectionViewViewModelClean_Factory create(
      Provider<BuscarColetasUseCase> buscarColetasUseCaseProvider,
      Provider<BuscarColetasComFallbackUseCase> buscarColetasComFallbackUseCaseProvider,
      Provider<ObterUsuarioAtualUseCase> obterUsuarioAtualUseCaseProvider,
      Provider<RemoverColetaUseCase> removerColetaUseCaseProvider,
      Provider<SincronizarColetasDoServidorUseCase> sincronizarColetasUseCaseProvider,
      Provider<ColetaMigration> coletaMigrationProvider,
      Provider<ReenviarColetaUseCase> reenviarColetaUseCaseProvider,
      Provider<ExcluirColetaPendenteUseCase> excluirColetaPendenteUseCaseProvider,
      Provider<BuscarInfoRelatorioFotoUseCase> buscarInfoRelatorioFotoUseCaseProvider,
      Provider<BaixarRelatorioFotoPdfUseCase> baixarRelatorioFotoPdfUseCaseProvider) {
    return new CollectionViewViewModelClean_Factory(buscarColetasUseCaseProvider, buscarColetasComFallbackUseCaseProvider, obterUsuarioAtualUseCaseProvider, removerColetaUseCaseProvider, sincronizarColetasUseCaseProvider, coletaMigrationProvider, reenviarColetaUseCaseProvider, excluirColetaPendenteUseCaseProvider, buscarInfoRelatorioFotoUseCaseProvider, baixarRelatorioFotoPdfUseCaseProvider);
  }

  public static CollectionViewViewModelClean newInstance(BuscarColetasUseCase buscarColetasUseCase,
      BuscarColetasComFallbackUseCase buscarColetasComFallbackUseCase,
      ObterUsuarioAtualUseCase obterUsuarioAtualUseCase, RemoverColetaUseCase removerColetaUseCase,
      SincronizarColetasDoServidorUseCase sincronizarColetasUseCase,
      ColetaMigration coletaMigration, ReenviarColetaUseCase reenviarColetaUseCase,
      ExcluirColetaPendenteUseCase excluirColetaPendenteUseCase,
      BuscarInfoRelatorioFotoUseCase buscarInfoRelatorioFotoUseCase,
      BaixarRelatorioFotoPdfUseCase baixarRelatorioFotoPdfUseCase) {
    return new CollectionViewViewModelClean(buscarColetasUseCase, buscarColetasComFallbackUseCase, obterUsuarioAtualUseCase, removerColetaUseCase, sincronizarColetasUseCase, coletaMigration, reenviarColetaUseCase, excluirColetaPendenteUseCase, buscarInfoRelatorioFotoUseCase, baixarRelatorioFotoPdfUseCase);
  }
}
