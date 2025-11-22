package com.inventario.mobile.data.repository;

import com.inventario.mobile.api.PatrimonioApi;
import com.inventario.mobile.api.SalaApi;
import com.inventario.mobile.data.local.dao.ColetaDao;
import com.inventario.mobile.data.local.dao.PatrimonioDao;
import com.inventario.mobile.data.local.dao.ResponsavelDao;
import com.inventario.mobile.data.local.dao.SalaDao;
import com.inventario.mobile.data.remote.api.ApiService;
import com.inventario.mobile.data.remote.api.ColetaApi;
import com.inventario.mobile.data.remote.api.OfflineSyncApi;
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
public final class SyncRepository_Factory implements Factory<SyncRepository> {
  private final Provider<PatrimonioDao> patrimonioDaoProvider;

  private final Provider<SalaDao> salaDaoProvider;

  private final Provider<ResponsavelDao> responsavelDaoProvider;

  private final Provider<ColetaDao> coletaDaoProvider;

  private final Provider<PatrimonioApi> patrimonioApiProvider;

  private final Provider<SalaApi> salaApiProvider;

  private final Provider<ColetaApi> coletaApiProvider;

  private final Provider<ApiService> apiServiceProvider;

  private final Provider<OfflineSyncApi> offlineSyncApiProvider;

  public SyncRepository_Factory(Provider<PatrimonioDao> patrimonioDaoProvider,
      Provider<SalaDao> salaDaoProvider, Provider<ResponsavelDao> responsavelDaoProvider,
      Provider<ColetaDao> coletaDaoProvider, Provider<PatrimonioApi> patrimonioApiProvider,
      Provider<SalaApi> salaApiProvider, Provider<ColetaApi> coletaApiProvider,
      Provider<ApiService> apiServiceProvider, Provider<OfflineSyncApi> offlineSyncApiProvider) {
    this.patrimonioDaoProvider = patrimonioDaoProvider;
    this.salaDaoProvider = salaDaoProvider;
    this.responsavelDaoProvider = responsavelDaoProvider;
    this.coletaDaoProvider = coletaDaoProvider;
    this.patrimonioApiProvider = patrimonioApiProvider;
    this.salaApiProvider = salaApiProvider;
    this.coletaApiProvider = coletaApiProvider;
    this.apiServiceProvider = apiServiceProvider;
    this.offlineSyncApiProvider = offlineSyncApiProvider;
  }

  @Override
  public SyncRepository get() {
    return newInstance(patrimonioDaoProvider.get(), salaDaoProvider.get(), responsavelDaoProvider.get(), coletaDaoProvider.get(), patrimonioApiProvider.get(), salaApiProvider.get(), coletaApiProvider.get(), apiServiceProvider.get(), offlineSyncApiProvider.get());
  }

  public static SyncRepository_Factory create(Provider<PatrimonioDao> patrimonioDaoProvider,
      Provider<SalaDao> salaDaoProvider, Provider<ResponsavelDao> responsavelDaoProvider,
      Provider<ColetaDao> coletaDaoProvider, Provider<PatrimonioApi> patrimonioApiProvider,
      Provider<SalaApi> salaApiProvider, Provider<ColetaApi> coletaApiProvider,
      Provider<ApiService> apiServiceProvider, Provider<OfflineSyncApi> offlineSyncApiProvider) {
    return new SyncRepository_Factory(patrimonioDaoProvider, salaDaoProvider, responsavelDaoProvider, coletaDaoProvider, patrimonioApiProvider, salaApiProvider, coletaApiProvider, apiServiceProvider, offlineSyncApiProvider);
  }

  public static SyncRepository newInstance(PatrimonioDao patrimonioDao, SalaDao salaDao,
      ResponsavelDao responsavelDao, ColetaDao coletaDao, PatrimonioApi patrimonioApi,
      SalaApi salaApi, ColetaApi coletaApi, ApiService apiService, OfflineSyncApi offlineSyncApi) {
    return new SyncRepository(patrimonioDao, salaDao, responsavelDao, coletaDao, patrimonioApi, salaApi, coletaApi, apiService, offlineSyncApi);
  }
}
