package com.inventario.mobile.data.repository;

import com.inventario.mobile.data.local.dao.ColetaDao;
import com.inventario.mobile.data.local.dao.DashboardDao;
import com.inventario.mobile.data.local.dao.PatrimonioDao;
import com.inventario.mobile.data.mapper.DashboardMapper;
import com.inventario.mobile.data.remote.api.ApiService;
import com.inventario.mobile.utils.PreferencesManager;
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

  private final Provider<PreferencesManager> preferencesManagerProvider;

  private final Provider<PatrimonioDao> patrimonioDaoProvider;

  private final Provider<ColetaDao> coletaDaoProvider;

  public DashboardRepositoryImpl_Factory(Provider<ApiService> apiServiceProvider,
      Provider<DashboardMapper> mapperProvider, Provider<DashboardDao> dashboardDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider,
      Provider<PatrimonioDao> patrimonioDaoProvider, Provider<ColetaDao> coletaDaoProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.mapperProvider = mapperProvider;
    this.dashboardDaoProvider = dashboardDaoProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
    this.patrimonioDaoProvider = patrimonioDaoProvider;
    this.coletaDaoProvider = coletaDaoProvider;
  }

  @Override
  public DashboardRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), mapperProvider.get(), dashboardDaoProvider.get(), preferencesManagerProvider.get(), patrimonioDaoProvider.get(), coletaDaoProvider.get());
  }

  public static DashboardRepositoryImpl_Factory create(Provider<ApiService> apiServiceProvider,
      Provider<DashboardMapper> mapperProvider, Provider<DashboardDao> dashboardDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider,
      Provider<PatrimonioDao> patrimonioDaoProvider, Provider<ColetaDao> coletaDaoProvider) {
    return new DashboardRepositoryImpl_Factory(apiServiceProvider, mapperProvider, dashboardDaoProvider, preferencesManagerProvider, patrimonioDaoProvider, coletaDaoProvider);
  }

  public static DashboardRepositoryImpl newInstance(ApiService apiService, DashboardMapper mapper,
      DashboardDao dashboardDao, PreferencesManager preferencesManager, PatrimonioDao patrimonioDao,
      ColetaDao coletaDao) {
    return new DashboardRepositoryImpl(apiService, mapper, dashboardDao, preferencesManager, patrimonioDao, coletaDao);
  }
}
