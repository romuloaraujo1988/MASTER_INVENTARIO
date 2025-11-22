package com.inventario.mobile.data.repository;

import com.inventario.mobile.data.audit.AuditService;
import com.inventario.mobile.data.local.dao.ColetaDao;
import com.inventario.mobile.data.local.dao.PatrimonioDao;
import com.inventario.mobile.data.mapper.ColetaMapper;
import com.inventario.mobile.data.remote.api.ColetaApi;
import com.inventario.mobile.data.remote.api.PatrimonioApi;
import com.inventario.mobile.network.NetworkQualityMonitor;
import com.inventario.mobile.utils.PreferencesManager;
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
public final class ColetaRepositoryImpl_Factory implements Factory<ColetaRepositoryImpl> {
  private final Provider<ColetaDao> coletaDaoProvider;

  private final Provider<PatrimonioDao> patrimonioDaoProvider;

  private final Provider<ColetaApi> coletaApiProvider;

  private final Provider<PatrimonioApi> patrimonioApiProvider;

  private final Provider<ColetaMapper> mapperProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  private final Provider<NetworkQualityMonitor> networkQualityMonitorProvider;

  private final Provider<AuditService> auditServiceProvider;

  public ColetaRepositoryImpl_Factory(Provider<ColetaDao> coletaDaoProvider,
      Provider<PatrimonioDao> patrimonioDaoProvider, Provider<ColetaApi> coletaApiProvider,
      Provider<PatrimonioApi> patrimonioApiProvider, Provider<ColetaMapper> mapperProvider,
      Provider<PreferencesManager> preferencesManagerProvider,
      Provider<NetworkQualityMonitor> networkQualityMonitorProvider,
      Provider<AuditService> auditServiceProvider) {
    this.coletaDaoProvider = coletaDaoProvider;
    this.patrimonioDaoProvider = patrimonioDaoProvider;
    this.coletaApiProvider = coletaApiProvider;
    this.patrimonioApiProvider = patrimonioApiProvider;
    this.mapperProvider = mapperProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
    this.networkQualityMonitorProvider = networkQualityMonitorProvider;
    this.auditServiceProvider = auditServiceProvider;
  }

  @Override
  public ColetaRepositoryImpl get() {
    return newInstance(coletaDaoProvider.get(), patrimonioDaoProvider.get(), coletaApiProvider.get(), patrimonioApiProvider.get(), mapperProvider.get(), preferencesManagerProvider.get(), networkQualityMonitorProvider.get(), auditServiceProvider.get());
  }

  public static ColetaRepositoryImpl_Factory create(Provider<ColetaDao> coletaDaoProvider,
      Provider<PatrimonioDao> patrimonioDaoProvider, Provider<ColetaApi> coletaApiProvider,
      Provider<PatrimonioApi> patrimonioApiProvider, Provider<ColetaMapper> mapperProvider,
      Provider<PreferencesManager> preferencesManagerProvider,
      Provider<NetworkQualityMonitor> networkQualityMonitorProvider,
      Provider<AuditService> auditServiceProvider) {
    return new ColetaRepositoryImpl_Factory(coletaDaoProvider, patrimonioDaoProvider, coletaApiProvider, patrimonioApiProvider, mapperProvider, preferencesManagerProvider, networkQualityMonitorProvider, auditServiceProvider);
  }

  public static ColetaRepositoryImpl newInstance(ColetaDao coletaDao, PatrimonioDao patrimonioDao,
      ColetaApi coletaApi, PatrimonioApi patrimonioApi, ColetaMapper mapper,
      PreferencesManager preferencesManager, NetworkQualityMonitor networkQualityMonitor,
      AuditService auditService) {
    return new ColetaRepositoryImpl(coletaDao, patrimonioDao, coletaApi, patrimonioApi, mapper, preferencesManager, networkQualityMonitor, auditService);
  }
}
