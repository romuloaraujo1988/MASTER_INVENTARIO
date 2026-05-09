package com.inventario.mobile.presentation.dashboard;

import com.inventario.mobile.domain.repository.DashboardRepository;
import com.inventario.mobile.domain.usecase.BuscarEstatisticasDashboardUseCase;
import com.inventario.mobile.domain.usecase.BuscarEvolucaoColetasUseCase;
import com.inventario.mobile.utils.PreferencesManager;
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
public final class DashboardViewModelClean_Factory implements Factory<DashboardViewModelClean> {
  private final Provider<BuscarEstatisticasDashboardUseCase> buscarEstatisticasDashboardUseCaseProvider;

  private final Provider<BuscarEvolucaoColetasUseCase> buscarEvolucaoColetasUseCaseProvider;

  private final Provider<DashboardRepository> dashboardRepositoryProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public DashboardViewModelClean_Factory(
      Provider<BuscarEstatisticasDashboardUseCase> buscarEstatisticasDashboardUseCaseProvider,
      Provider<BuscarEvolucaoColetasUseCase> buscarEvolucaoColetasUseCaseProvider,
      Provider<DashboardRepository> dashboardRepositoryProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.buscarEstatisticasDashboardUseCaseProvider = buscarEstatisticasDashboardUseCaseProvider;
    this.buscarEvolucaoColetasUseCaseProvider = buscarEvolucaoColetasUseCaseProvider;
    this.dashboardRepositoryProvider = dashboardRepositoryProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  @Override
  public DashboardViewModelClean get() {
    return newInstance(buscarEstatisticasDashboardUseCaseProvider.get(), buscarEvolucaoColetasUseCaseProvider.get(), dashboardRepositoryProvider.get(), preferencesManagerProvider.get());
  }

  public static DashboardViewModelClean_Factory create(
      Provider<BuscarEstatisticasDashboardUseCase> buscarEstatisticasDashboardUseCaseProvider,
      Provider<BuscarEvolucaoColetasUseCase> buscarEvolucaoColetasUseCaseProvider,
      Provider<DashboardRepository> dashboardRepositoryProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new DashboardViewModelClean_Factory(buscarEstatisticasDashboardUseCaseProvider, buscarEvolucaoColetasUseCaseProvider, dashboardRepositoryProvider, preferencesManagerProvider);
  }

  public static DashboardViewModelClean newInstance(
      BuscarEstatisticasDashboardUseCase buscarEstatisticasDashboardUseCase,
      BuscarEvolucaoColetasUseCase buscarEvolucaoColetasUseCase,
      DashboardRepository dashboardRepository, PreferencesManager preferencesManager) {
    return new DashboardViewModelClean(buscarEstatisticasDashboardUseCase, buscarEvolucaoColetasUseCase, dashboardRepository, preferencesManager);
  }
}
