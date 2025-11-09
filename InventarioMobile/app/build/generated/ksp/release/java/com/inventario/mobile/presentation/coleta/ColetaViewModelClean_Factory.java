package com.inventario.mobile.presentation.coleta;

import com.inventario.mobile.domain.usecase.BuscarPatrimonioUseCase;
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase;
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
public final class ColetaViewModelClean_Factory implements Factory<ColetaViewModelClean> {
  private final Provider<BuscarPatrimonioUseCase> buscarPatrimonioUseCaseProvider;

  private final Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider;

  public ColetaViewModelClean_Factory(
      Provider<BuscarPatrimonioUseCase> buscarPatrimonioUseCaseProvider,
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider) {
    this.buscarPatrimonioUseCaseProvider = buscarPatrimonioUseCaseProvider;
    this.registrarColetaUseCaseProvider = registrarColetaUseCaseProvider;
  }

  @Override
  public ColetaViewModelClean get() {
    return newInstance(buscarPatrimonioUseCaseProvider.get(), registrarColetaUseCaseProvider.get());
  }

  public static ColetaViewModelClean_Factory create(
      Provider<BuscarPatrimonioUseCase> buscarPatrimonioUseCaseProvider,
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider) {
    return new ColetaViewModelClean_Factory(buscarPatrimonioUseCaseProvider, registrarColetaUseCaseProvider);
  }

  public static ColetaViewModelClean newInstance(BuscarPatrimonioUseCase buscarPatrimonioUseCase,
      RegistrarColetaUseCase registrarColetaUseCase) {
    return new ColetaViewModelClean(buscarPatrimonioUseCase, registrarColetaUseCase);
  }
}
