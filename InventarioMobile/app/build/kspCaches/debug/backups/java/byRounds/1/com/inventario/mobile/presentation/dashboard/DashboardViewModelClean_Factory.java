package com.inventario.mobile.presentation.dashboard;

import com.inventario.mobile.data.repository.DashboardRepositoryImpl;
import com.inventario.mobile.domain.usecase.BuscarEstatisticasDashboardUseCase;
import com.inventario.mobile.domain.usecase.BuscarEvolucaoColetasUseCase;
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

  private final Provider<DashboardRepositoryImpl> dashboardRepositoryProvider;

  public DashboardViewModelClean_Factory(
      Provider<BuscarEstatisticasDashboardUseCase> buscarEstatisticasDashboardUseCaseProvider,
      Provider<BuscarEvolucaoColetasUseCase> buscarEvolucaoColetasUseCaseProvider,
      Provider<DashboardRepositoryImpl> dashboardRepositoryProvider) {
    this.buscarEstatisticasDashboardUseCaseProvider = buscarEstatisticasDashboardUseCaseProvider;
    this.buscarEvolucaoColetasUseCaseProvider = buscarEvolucaoColetasUseCaseProvider;
    this.dashboardRepositoryProvider = dashboardRepositoryProvider;
  }

  @Override
  public DashboardViewModelClean get() {
    return newInstance(buscarEstatisticasDashboardUseCaseProvider.get(), buscarEvolucaoColetasUseCaseProvider.get(), dashboardRepositoryProvider.get());
  }

  public static DashboardViewModelClean_Factory create(
      Provider<BuscarEstatisticasDashboardUseCase> buscarEstatisticasDashboardUseCaseProvider,
      Provider<BuscarEvolucaoColetasUseCase> buscarEvolucaoColetasUseCaseProvider,
      Provider<DashboardRepositoryImpl> dashboardRepositoryProvider) {
    return new DashboardViewModelClean_Factory(buscarEstatisticasDashboardUseCaseProvider, buscarEvolucaoColetasUseCaseProvider, dashboardRepositoryProvider);
  }

  public static DashboardViewModelClean newInstance(
      BuscarEstatisticasDashboardUseCase buscarEstatisticasDashboardUseCase,
      BuscarEvolucaoColetasUseCase buscarEvolucaoColetasUseCase,
      DashboardRepositoryImpl dashboardRepository) {
    return new DashboardViewModelClean(buscarEstatisticasDashboardUseCase, buscarEvolucaoColetasUseCase, dashboardRepository);
  }
}
