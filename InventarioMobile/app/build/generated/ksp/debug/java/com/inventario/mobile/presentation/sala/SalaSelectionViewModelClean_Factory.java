package com.inventario.mobile.presentation.sala;

import com.inventario.mobile.domain.usecase.BuscarSalasUseCase;
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
public final class SalaSelectionViewModelClean_Factory implements Factory<SalaSelectionViewModelClean> {
  private final Provider<BuscarSalasUseCase> buscarSalasUseCaseProvider;

  public SalaSelectionViewModelClean_Factory(
      Provider<BuscarSalasUseCase> buscarSalasUseCaseProvider) {
    this.buscarSalasUseCaseProvider = buscarSalasUseCaseProvider;
  }

  @Override
  public SalaSelectionViewModelClean get() {
    return newInstance(buscarSalasUseCaseProvider.get());
  }

  public static SalaSelectionViewModelClean_Factory create(
      Provider<BuscarSalasUseCase> buscarSalasUseCaseProvider) {
    return new SalaSelectionViewModelClean_Factory(buscarSalasUseCaseProvider);
  }

  public static SalaSelectionViewModelClean newInstance(BuscarSalasUseCase buscarSalasUseCase) {
    return new SalaSelectionViewModelClean(buscarSalasUseCase);
  }
}
