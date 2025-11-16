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
public final class SalaViewModelPaging_Factory implements Factory<SalaViewModelPaging> {
  private final Provider<BuscarSalasUseCase> buscarSalasUseCaseProvider;

  public SalaViewModelPaging_Factory(Provider<BuscarSalasUseCase> buscarSalasUseCaseProvider) {
    this.buscarSalasUseCaseProvider = buscarSalasUseCaseProvider;
  }

  @Override
  public SalaViewModelPaging get() {
    return newInstance(buscarSalasUseCaseProvider.get());
  }

  public static SalaViewModelPaging_Factory create(
      Provider<BuscarSalasUseCase> buscarSalasUseCaseProvider) {
    return new SalaViewModelPaging_Factory(buscarSalasUseCaseProvider);
  }

  public static SalaViewModelPaging newInstance(BuscarSalasUseCase buscarSalasUseCase) {
    return new SalaViewModelPaging(buscarSalasUseCase);
  }
}
