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
public final class BuscarTopItensUseCase_Factory implements Factory<BuscarTopItensUseCase> {
  private final Provider<DashboardRepository> dashboardRepositoryProvider;

  public BuscarTopItensUseCase_Factory(Provider<DashboardRepository> dashboardRepositoryProvider) {
    this.dashboardRepositoryProvider = dashboardRepositoryProvider;
  }

  @Override
  public BuscarTopItensUseCase get() {
    return newInstance(dashboardRepositoryProvider.get());
  }

  public static BuscarTopItensUseCase_Factory create(
      Provider<DashboardRepository> dashboardRepositoryProvider) {
    return new BuscarTopItensUseCase_Factory(dashboardRepositoryProvider);
  }

  public static BuscarTopItensUseCase newInstance(DashboardRepository dashboardRepository) {
    return new BuscarTopItensUseCase(dashboardRepository);
  }
}
