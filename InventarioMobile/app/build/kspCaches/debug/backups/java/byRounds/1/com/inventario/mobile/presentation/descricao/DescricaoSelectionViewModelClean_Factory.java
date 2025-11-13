package com.inventario.mobile.presentation.descricao;

import com.inventario.mobile.domain.usecase.BuscarDescricoesNaoColetadasUseCase;
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
public final class DescricaoSelectionViewModelClean_Factory implements Factory<DescricaoSelectionViewModelClean> {
  private final Provider<BuscarDescricoesNaoColetadasUseCase> buscarDescricoesNaoColetadasUseCaseProvider;

  public DescricaoSelectionViewModelClean_Factory(
      Provider<BuscarDescricoesNaoColetadasUseCase> buscarDescricoesNaoColetadasUseCaseProvider) {
    this.buscarDescricoesNaoColetadasUseCaseProvider = buscarDescricoesNaoColetadasUseCaseProvider;
  }

  @Override
  public DescricaoSelectionViewModelClean get() {
    return newInstance(buscarDescricoesNaoColetadasUseCaseProvider.get());
  }

  public static DescricaoSelectionViewModelClean_Factory create(
      Provider<BuscarDescricoesNaoColetadasUseCase> buscarDescricoesNaoColetadasUseCaseProvider) {
    return new DescricaoSelectionViewModelClean_Factory(buscarDescricoesNaoColetadasUseCaseProvider);
  }

  public static DescricaoSelectionViewModelClean newInstance(
      BuscarDescricoesNaoColetadasUseCase buscarDescricoesNaoColetadasUseCase) {
    return new DescricaoSelectionViewModelClean(buscarDescricoesNaoColetadasUseCase);
  }
}
