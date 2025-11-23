package com.inventario.mobile.data.repository;

import com.inventario.mobile.data.local.dao.DashboardDao;
import com.inventario.mobile.data.mapper.DashboardMapper;
import com.inventario.mobile.data.remote.api.ApiService;
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
public final class DashboardRepositoryImpl_Factory implements Factory<DashboardRepositoryImpl> {
  private final Provider<ApiService> apiServiceProvider;

  private final Provider<DashboardMapper> mapperProvider;

  private final Provider<DashboardDao> dashboardDaoProvider;

  public DashboardRepositoryImpl_Factory(Provider<ApiService> apiServiceProvider,
      Provider<DashboardMapper> mapperProvider, Provider<DashboardDao> dashboardDaoProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.mapperProvider = mapperProvider;
    this.dashboardDaoProvider = dashboardDaoProvider;
  }

  @Override
  public DashboardRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), mapperProvider.get(), dashboardDaoProvider.get());
  }

  public static DashboardRepositoryImpl_Factory create(Provider<ApiService> apiServiceProvider,
      Provider<DashboardMapper> mapperProvider, Provider<DashboardDao> dashboardDaoProvider) {
    return new DashboardRepositoryImpl_Factory(apiServiceProvider, mapperProvider, dashboardDaoProvider);
  }

  public static DashboardRepositoryImpl newInstance(ApiService apiService, DashboardMapper mapper,
      DashboardDao dashboardDao) {
    return new DashboardRepositoryImpl(apiService, mapper, dashboardDao);
  }
}
