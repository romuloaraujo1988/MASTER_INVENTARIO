package com.inventario.mobile.data.repository;

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

  public DashboardRepositoryImpl_Factory(Provider<ApiService> apiServiceProvider,
      Provider<DashboardMapper> mapperProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.mapperProvider = mapperProvider;
  }

  @Override
  public DashboardRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), mapperProvider.get());
  }

  public static DashboardRepositoryImpl_Factory create(Provider<ApiService> apiServiceProvider,
      Provider<DashboardMapper> mapperProvider) {
    return new DashboardRepositoryImpl_Factory(apiServiceProvider, mapperProvider);
  }

  public static DashboardRepositoryImpl newInstance(ApiService apiService, DashboardMapper mapper) {
    return new DashboardRepositoryImpl(apiService, mapper);
  }
}
