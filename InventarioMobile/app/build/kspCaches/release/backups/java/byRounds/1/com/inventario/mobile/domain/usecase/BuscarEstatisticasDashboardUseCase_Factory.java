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
public final class BuscarEstatisticasDashboardUseCase_Factory implements Factory<BuscarEstatisticasDashboardUseCase> {
  private final Provider<DashboardRepository> dashboardRepositoryProvider;

  public BuscarEstatisticasDashboardUseCase_Factory(
      Provider<DashboardRepository> dashboardRepositoryProvider) {
    this.dashboardRepositoryProvider = dashboardRepositoryProvider;
  }

  @Override
  public BuscarEstatisticasDashboardUseCase get() {
    return newInstance(dashboardRepositoryProvider.get());
  }

  public static BuscarEstatisticasDashboardUseCase_Factory create(
      Provider<DashboardRepository> dashboardRepositoryProvider) {
    return new BuscarEstatisticasDashboardUseCase_Factory(dashboardRepositoryProvider);
  }

  public static BuscarEstatisticasDashboardUseCase newInstance(
      DashboardRepository dashboardRepository) {
    return new BuscarEstatisticasDashboardUseCase(dashboardRepository);
  }
}
