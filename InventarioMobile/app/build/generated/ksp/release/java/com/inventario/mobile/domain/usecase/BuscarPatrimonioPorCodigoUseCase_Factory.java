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
public final class BuscarPatrimonioPorCodigoUseCase_Factory implements Factory<BuscarPatrimonioPorCodigoUseCase> {
  private final Provider<PatrimonioConsultaRepository> repositoryProvider;

  public BuscarPatrimonioPorCodigoUseCase_Factory(
      Provider<PatrimonioConsultaRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public BuscarPatrimonioPorCodigoUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static BuscarPatrimonioPorCodigoUseCase_Factory create(
      Provider<PatrimonioConsultaRepository> repositoryProvider) {
    return new BuscarPatrimonioPorCodigoUseCase_Factory(repositoryProvider);
  }

  public static BuscarPatrimonioPorCodigoUseCase newInstance(
      PatrimonioConsultaRepository repository) {
    return new BuscarPatrimonioPorCodigoUseCase(repository);
  }
}
