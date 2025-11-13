package com.inventario.mobile.presentation.coleta;

import com.inventario.mobile.domain.usecase.BuscarPatrimonioUseCase;
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase;
import com.inventario.mobile.sync.SyncScheduler;
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

  private final Provider<SyncScheduler> syncSchedulerProvider;

  public ColetaViewModelClean_Factory(
      Provider<BuscarPatrimonioUseCase> buscarPatrimonioUseCaseProvider,
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider,
      Provider<SyncScheduler> syncSchedulerProvider) {
    this.buscarPatrimonioUseCaseProvider = buscarPatrimonioUseCaseProvider;
    this.registrarColetaUseCaseProvider = registrarColetaUseCaseProvider;
    this.syncSchedulerProvider = syncSchedulerProvider;
  }

  @Override
  public ColetaViewModelClean get() {
    return newInstance(buscarPatrimonioUseCaseProvider.get(), registrarColetaUseCaseProvider.get(), syncSchedulerProvider.get());
  }

  public static ColetaViewModelClean_Factory create(
      Provider<BuscarPatrimonioUseCase> buscarPatrimonioUseCaseProvider,
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider,
      Provider<SyncScheduler> syncSchedulerProvider) {
    return new ColetaViewModelClean_Factory(buscarPatrimonioUseCaseProvider, registrarColetaUseCaseProvider, syncSchedulerProvider);
  }

  public static ColetaViewModelClean newInstance(BuscarPatrimonioUseCase buscarPatrimonioUseCase,
      RegistrarColetaUseCase registrarColetaUseCase, SyncScheduler syncScheduler) {
    return new ColetaViewModelClean(buscarPatrimonioUseCase, registrarColetaUseCase, syncScheduler);
  }
}
