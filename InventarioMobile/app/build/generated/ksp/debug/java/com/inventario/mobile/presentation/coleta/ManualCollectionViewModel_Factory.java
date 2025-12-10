package com.inventario.mobile.presentation.coleta;

import com.inventario.mobile.data.local.dao.ColetaDao;
import com.inventario.mobile.data.repository.InventarioRepository;
import com.inventario.mobile.domain.usecase.BuscarPatrimonioUseCase;
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase;
import com.inventario.mobile.utils.VibrationHelper;
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

  private final Provider<ColetaDao> coletaDaoProvider;

  private final Provider<VibrationHelper> vibrationHelperProvider;

  public ManualCollectionViewModel_Factory(
      Provider<BuscarPatrimonioUseCase> buscarPatrimonioUseCaseProvider,
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider,
      Provider<InventarioRepository> inventarioRepositoryProvider,
      Provider<ColetaDao> coletaDaoProvider, Provider<VibrationHelper> vibrationHelperProvider) {
    this.buscarPatrimonioUseCaseProvider = buscarPatrimonioUseCaseProvider;
    this.registrarColetaUseCaseProvider = registrarColetaUseCaseProvider;
    this.inventarioRepositoryProvider = inventarioRepositoryProvider;
    this.coletaDaoProvider = coletaDaoProvider;
    this.vibrationHelperProvider = vibrationHelperProvider;
  }

  @Override
  public ManualCollectionViewModel get() {
    return newInstance(buscarPatrimonioUseCaseProvider.get(), registrarColetaUseCaseProvider.get(), inventarioRepositoryProvider.get(), coletaDaoProvider.get(), vibrationHelperProvider.get());
  }

  public static ManualCollectionViewModel_Factory create(
      Provider<BuscarPatrimonioUseCase> buscarPatrimonioUseCaseProvider,
      Provider<RegistrarColetaUseCase> registrarColetaUseCaseProvider,
      Provider<InventarioRepository> inventarioRepositoryProvider,
      Provider<ColetaDao> coletaDaoProvider, Provider<VibrationHelper> vibrationHelperProvider) {
    return new ManualCollectionViewModel_Factory(buscarPatrimonioUseCaseProvider, registrarColetaUseCaseProvider, inventarioRepositoryProvider, coletaDaoProvider, vibrationHelperProvider);
  }

  public static ManualCollectionViewModel newInstance(
      BuscarPatrimonioUseCase buscarPatrimonioUseCase,
      RegistrarColetaUseCase registrarColetaUseCase, InventarioRepository inventarioRepository,
      ColetaDao coletaDao, VibrationHelper vibrationHelper) {
    return new ManualCollectionViewModel(buscarPatrimonioUseCase, registrarColetaUseCase, inventarioRepository, coletaDao, vibrationHelper);
  }
}
