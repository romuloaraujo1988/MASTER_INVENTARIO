package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.domain.repository.SalaRepository;
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
public final class BuscarSalasUseCase_Factory implements Factory<BuscarSalasUseCase> {
  private final Provider<SalaRepository> salaRepositoryProvider;

  public BuscarSalasUseCase_Factory(Provider<SalaRepository> salaRepositoryProvider) {
    this.salaRepositoryProvider = salaRepositoryProvider;
  }

  @Override
  public BuscarSalasUseCase get() {
    return newInstance(salaRepositoryProvider.get());
  }

  public static BuscarSalasUseCase_Factory create(Provider<SalaRepository> salaRepositoryProvider) {
    return new BuscarSalasUseCase_Factory(salaRepositoryProvider);
  }

  public static BuscarSalasUseCase newInstance(SalaRepository salaRepository) {
    return new BuscarSalasUseCase(salaRepository);
  }
}
