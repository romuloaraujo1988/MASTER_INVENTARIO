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
public final class BuscarPatrimonioUseCase_Factory implements Factory<BuscarPatrimonioUseCase> {
  private final Provider<PatrimonioRepository> patrimonioRepositoryProvider;

  public BuscarPatrimonioUseCase_Factory(
      Provider<PatrimonioRepository> patrimonioRepositoryProvider) {
    this.patrimonioRepositoryProvider = patrimonioRepositoryProvider;
  }

  @Override
  public BuscarPatrimonioUseCase get() {
    return newInstance(patrimonioRepositoryProvider.get());
  }

  public static BuscarPatrimonioUseCase_Factory create(
      Provider<PatrimonioRepository> patrimonioRepositoryProvider) {
    return new BuscarPatrimonioUseCase_Factory(patrimonioRepositoryProvider);
  }

  public static BuscarPatrimonioUseCase newInstance(PatrimonioRepository patrimonioRepository) {
    return new BuscarPatrimonioUseCase(patrimonioRepository);
  }
}
