package com.inventario.mobile.di;

import com.inventario.mobile.data.mapper.DashboardMapper;
import com.inventario.mobile.data.remote.api.ApiService;
import com.inventario.mobile.domain.repository.DashboardRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DashboardModule_ProvideDashboardRepositoryFactory implements Factory<DashboardRepository> {
  private final Provider<ApiService> apiServiceProvider;

  private final Provider<DashboardMapper> mapperProvider;

  public DashboardModule_ProvideDashboardRepositoryFactory(Provider<ApiService> apiServiceProvider,
      Provider<DashboardMapper> mapperProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.mapperProvider = mapperProvider;
  }

  @Override
  public DashboardRepository get() {
    return provideDashboardRepository(apiServiceProvider.get(), mapperProvider.get());
  }

  public static DashboardModule_ProvideDashboardRepositoryFactory create(
      Provider<ApiService> apiServiceProvider, Provider<DashboardMapper> mapperProvider) {
    return new DashboardModule_ProvideDashboardRepositoryFactory(apiServiceProvider, mapperProvider);
  }

  public static DashboardRepository provideDashboardRepository(ApiService apiService,
      DashboardMapper mapper) {
    return Preconditions.checkNotNullFromProvides(DashboardModule.INSTANCE.provideDashboardRepository(apiService, mapper));
  }
}
