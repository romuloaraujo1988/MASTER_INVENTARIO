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
public final class RemoverColetaUseCase_Factory implements Factory<RemoverColetaUseCase> {
  private final Provider<ColetaRepository> coletaRepositoryProvider;

  public RemoverColetaUseCase_Factory(Provider<ColetaRepository> coletaRepositoryProvider) {
    this.coletaRepositoryProvider = coletaRepositoryProvider;
  }

  @Override
  public RemoverColetaUseCase get() {
    return newInstance(coletaRepositoryProvider.get());
  }

  public static RemoverColetaUseCase_Factory create(
      Provider<ColetaRepository> coletaRepositoryProvider) {
    return new RemoverColetaUseCase_Factory(coletaRepositoryProvider);
  }

  public static RemoverColetaUseCase newInstance(ColetaRepository coletaRepository) {
    return new RemoverColetaUseCase(coletaRepository);
  }
}
