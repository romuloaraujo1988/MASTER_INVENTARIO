package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.domain.repository.PatrimonioConsultaRepository;
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
public final class BuscarPatrimonioAvancadaUseCase_Factory implements Factory<BuscarPatrimonioAvancadaUseCase> {
  private final Provider<PatrimonioConsultaRepository> repositoryProvider;

  public BuscarPatrimonioAvancadaUseCase_Factory(
      Provider<PatrimonioConsultaRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public BuscarPatrimonioAvancadaUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static BuscarPatrimonioAvancadaUseCase_Factory create(
      Provider<PatrimonioConsultaRepository> repositoryProvider) {
    return new BuscarPatrimonioAvancadaUseCase_Factory(repositoryProvider);
  }

  public static BuscarPatrimonioAvancadaUseCase newInstance(
      PatrimonioConsultaRepository repository) {
    return new BuscarPatrimonioAvancadaUseCase(repository);
  }
}
