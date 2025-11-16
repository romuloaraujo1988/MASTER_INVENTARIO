package com.inventario.mobile.presentation.coleta;

import com.inventario.mobile.data.repository.InventarioRepository;
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
public final class ManualCollectionViewModel_Factory implements Factory<ManualCollectionViewModel> {
  private final Provider<BuscarPatrimonioUseCase> buscarPatrimonioUseCaseProvider;

  private final Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider;

  private final Provider<InventarioRepository> inventarioRepositoryProvider;

  public ManualCollectionViewModel_Factory(
      Provider<BuscarPatrimonioUseCase> buscarPatrimonioUseCaseProvider,
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider,
      Provider<InventarioRepository> inventarioRepositoryProvider) {
    this.buscarPatrimonioUseCaseProvider = buscarPatrimonioUseCaseProvider;
    this.registrarColetaUseCaseProvider = registrarColetaUseCaseProvider;
    this.inventarioRepositoryProvider = inventarioRepositoryProvider;
  }

  @Override
  public ManualCollectionViewModel get() {
    return newInstance(buscarPatrimonioUseCaseProvider.get(), registrarColetaUseCaseProvider.get(), inventarioRepositoryProvider.get());
  }

  public static ManualCollectionViewModel_Factory create(
      Provider<BuscarPatrimonioUseCase> buscarPatrimonioUseCaseProvider,
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider,
      Provider<InventarioRepository> inventarioRepositoryProvider) {
    return new ManualCollectionViewModel_Factory(buscarPatrimonioUseCaseProvider, registrarColetaUseCaseProvider, inventarioRepositoryProvider);
  }

  public static ManualCollectionViewModel newInstance(
      BuscarPatrimonioUseCase buscarPatrimonioUseCase,
      RegistrarColetaUseCase registrarColetaUseCase, InventarioRepository inventarioRepository) {
    return new ManualCollectionViewModel(buscarPatrimonioUseCase, registrarColetaUseCase, inventarioRepository);
  }
}
