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
public final class BuscarPatrimoniosPorDescricaoUseCase_Factory implements Factory<BuscarPatrimoniosPorDescricaoUseCase> {
  private final Provider<PatrimonioRepository> patrimonioRepositoryProvider;

  public BuscarPatrimoniosPorDescricaoUseCase_Factory(
      Provider<PatrimonioRepository> patrimonioRepositoryProvider) {
    this.patrimonioRepositoryProvider = patrimonioRepositoryProvider;
  }

  @Override
  public BuscarPatrimoniosPorDescricaoUseCase get() {
    return newInstance(patrimonioRepositoryProvider.get());
  }

  public static BuscarPatrimoniosPorDescricaoUseCase_Factory create(
      Provider<PatrimonioRepository> patrimonioRepositoryProvider) {
    return new BuscarPatrimoniosPorDescricaoUseCase_Factory(patrimonioRepositoryProvider);
  }

  public static BuscarPatrimoniosPorDescricaoUseCase newInstance(
      PatrimonioRepository patrimonioRepository) {
    return new BuscarPatrimoniosPorDescricaoUseCase(patrimonioRepository);
  }
}
