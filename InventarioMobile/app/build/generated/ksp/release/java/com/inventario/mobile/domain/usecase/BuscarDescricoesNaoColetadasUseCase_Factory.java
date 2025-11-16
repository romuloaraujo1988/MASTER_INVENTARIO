package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.domain.repository.PatrimonioRepository;
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
public final class BuscarDescricoesNaoColetadasUseCase_Factory implements Factory<BuscarDescricoesNaoColetadasUseCase> {
  private final Provider<PatrimonioRepository> patrimonioRepositoryProvider;

  public BuscarDescricoesNaoColetadasUseCase_Factory(
      Provider<PatrimonioRepository> patrimonioRepositoryProvider) {
    this.patrimonioRepositoryProvider = patrimonioRepositoryProvider;
  }

  @Override
  public BuscarDescricoesNaoColetadasUseCase get() {
    return newInstance(patrimonioRepositoryProvider.get());
  }

  public static BuscarDescricoesNaoColetadasUseCase_Factory create(
      Provider<PatrimonioRepository> patrimonioRepositoryProvider) {
    return new BuscarDescricoesNaoColetadasUseCase_Factory(patrimonioRepositoryProvider);
  }

  public static BuscarDescricoesNaoColetadasUseCase newInstance(
      PatrimonioRepository patrimonioRepository) {
    return new BuscarDescricoesNaoColetadasUseCase(patrimonioRepository);
  }
}
