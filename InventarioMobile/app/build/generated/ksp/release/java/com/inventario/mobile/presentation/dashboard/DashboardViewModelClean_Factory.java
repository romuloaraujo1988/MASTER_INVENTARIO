package com.inventario.mobile.presentation.dashboard;

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

  public DashboardViewModelClean_Factory(
      Provider<BuscarEstatisticasDashboardUseCase> buscarEstatisticasDashboardUseCaseProvider,
      Provider<BuscarEvolucaoColetasUseCase> buscarEvolucaoColetasUseCaseProvider) {
    this.buscarEstatisticasDashboardUseCaseProvider = buscarEstatisticasDashboardUseCaseProvider;
    this.buscarEvolucaoColetasUseCaseProvider = buscarEvolucaoColetasUseCaseProvider;
  }

  @Override
  public DashboardViewModelClean get() {
    return newInstance(buscarEstatisticasDashboardUseCaseProvider.get(), buscarEvolucaoColetasUseCaseProvider.get());
  }

  public static DashboardViewModelClean_Factory create(
      Provider<BuscarEstatisticasDashboardUseCase> buscarEstatisticasDashboardUseCaseProvider,
      Provider<BuscarEvolucaoColetasUseCase> buscarEvolucaoColetasUseCaseProvider) {
    return new DashboardViewModelClean_Factory(buscarEstatisticasDashboardUseCaseProvider, buscarEvolucaoColetasUseCaseProvider);
  }

  public static DashboardViewModelClean newInstance(
      BuscarEstatisticasDashboardUseCase buscarEstatisticasDashboardUseCase,
      BuscarEvolucaoColetasUseCase buscarEvolucaoColetasUseCase) {
    return new DashboardViewModelClean(buscarEstatisticasDashboardUseCase, buscarEvolucaoColetasUseCase);
  }
}
