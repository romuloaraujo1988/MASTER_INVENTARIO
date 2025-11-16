package com.inventario.mobile.domain.usecase;

import com.inventario.mobile.domain.repository.DashboardRepository;
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
public final class BuscarEvolucaoColetasUseCase_Factory implements Factory<BuscarEvolucaoColetasUseCase> {
  private final Provider<DashboardRepository> dashboardRepositoryProvider;

  public BuscarEvolucaoColetasUseCase_Factory(
      Provider<DashboardRepository> dashboardRepositoryProvider) {
    this.dashboardRepositoryProvider = dashboardRepositoryProvider;
  }

  @Override
  public BuscarEvolucaoColetasUseCase get() {
    return newInstance(dashboardRepositoryProvider.get());
  }

  public static BuscarEvolucaoColetasUseCase_Factory create(
      Provider<DashboardRepository> dashboardRepositoryProvider) {
    return new BuscarEvolucaoColetasUseCase_Factory(dashboardRepositoryProvider);
  }

  public static BuscarEvolucaoColetasUseCase newInstance(DashboardRepository dashboardRepository) {
    return new BuscarEvolucaoColetasUseCase(dashboardRepository);
  }
}
