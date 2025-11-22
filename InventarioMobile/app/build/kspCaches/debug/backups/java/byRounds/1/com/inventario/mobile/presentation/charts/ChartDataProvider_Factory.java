package com.inventario.mobile.presentation.charts;

import com.inventario.mobile.domain.repository.DashboardRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ChartDataProvider_Factory implements Factory<ChartDataProvider> {
  private final Provider<DashboardRepository> dashboardRepositoryProvider;

  public ChartDataProvider_Factory(Provider<DashboardRepository> dashboardRepositoryProvider) {
    this.dashboardRepositoryProvider = dashboardRepositoryProvider;
  }

  @Override
  public ChartDataProvider get() {
    return newInstance(dashboardRepositoryProvider.get());
  }

  public static ChartDataProvider_Factory create(
      Provider<DashboardRepository> dashboardRepositoryProvider) {
    return new ChartDataProvider_Factory(dashboardRepositoryProvider);
  }

  public static ChartDataProvider newInstance(DashboardRepository dashboardRepository) {
    return new ChartDataProvider(dashboardRepository);
  }
}
