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
public final class BuscarPatrimonioPorDescricaoUseCase_Factory implements Factory<BuscarPatrimonioPorDescricaoUseCase> {
  private final Provider<PatrimonioConsultaRepository> repositoryProvider;

  public BuscarPatrimonioPorDescricaoUseCase_Factory(
      Provider<PatrimonioConsultaRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public BuscarPatrimonioPorDescricaoUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static BuscarPatrimonioPorDescricaoUseCase_Factory create(
      Provider<PatrimonioConsultaRepository> repositoryProvider) {
    return new BuscarPatrimonioPorDescricaoUseCase_Factory(repositoryProvider);
  }

  public static BuscarPatrimonioPorDescricaoUseCase newInstance(
      PatrimonioConsultaRepository repository) {
    return new BuscarPatrimonioPorDescricaoUseCase(repository);
  }
}
