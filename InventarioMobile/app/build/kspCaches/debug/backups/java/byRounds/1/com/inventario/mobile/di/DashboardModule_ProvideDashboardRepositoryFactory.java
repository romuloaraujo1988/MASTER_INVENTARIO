package com.inventario.mobile.di;

import com.inventario.mobile.data.local.dao.DashboardDao;
import com.inventario.mobile.data.mapper.DashboardMapper;
import com.inventario.mobile.data.remote.api.ApiService;
import com.inventario.mobile.domain.repository.DashboardRepository;
import com.inventario.mobile.utils.PreferencesManager;
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

  private final Provider<DashboardDao> dashboardDaoProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public DashboardModule_ProvideDashboardRepositoryFactory(Provider<ApiService> apiServiceProvider,
      Provider<DashboardMapper> mapperProvider, Provider<DashboardDao> dashboardDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.mapperProvider = mapperProvider;
    this.dashboardDaoProvider = dashboardDaoProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  @Override
  public DashboardRepository get() {
    return provideDashboardRepository(apiServiceProvider.get(), mapperProvider.get(), dashboardDaoProvider.get(), preferencesManagerProvider.get());
  }

  public static DashboardModule_ProvideDashboardRepositoryFactory create(
      Provider<ApiService> apiServiceProvider, Provider<DashboardMapper> mapperProvider,
      Provider<DashboardDao> dashboardDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new DashboardModule_ProvideDashboardRepositoryFactory(apiServiceProvider, mapperProvider, dashboardDaoProvider, preferencesManagerProvider);
  }

  public static DashboardRepository provideDashboardRepository(ApiService apiService,
      DashboardMapper mapper, DashboardDao dashboardDao, PreferencesManager preferencesManager) {
    return Preconditions.checkNotNullFromProvides(DashboardModule.INSTANCE.provideDashboardRepository(apiService, mapper, dashboardDao, preferencesManager));
  }
}
