package com.inventario.mobile.data.repository;

import com.inventario.mobile.data.local.dao.ColetaDao;
import com.inventario.mobile.data.local.dao.PatrimonioDao;
import com.inventario.mobile.data.local.dao.SalaDao;
import com.inventario.mobile.data.remote.api.ColetaApi;
import com.inventario.mobile.data.remote.api.PatrimonioApi;
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

  private final Provider<ColetaDao> coletaDaoProvider;

  private final Provider<PatrimonioApi> patrimonioApiProvider;

  private final Provider<ColetaApi> coletaApiProvider;

  public SyncRepository_Factory(Provider<PatrimonioDao> patrimonioDaoProvider,
      Provider<SalaDao> salaDaoProvider, Provider<ColetaDao> coletaDaoProvider,
      Provider<PatrimonioApi> patrimonioApiProvider, Provider<ColetaApi> coletaApiProvider) {
    this.patrimonioDaoProvider = patrimonioDaoProvider;
    this.salaDaoProvider = salaDaoProvider;
    this.coletaDaoProvider = coletaDaoProvider;
    this.patrimonioApiProvider = patrimonioApiProvider;
    this.coletaApiProvider = coletaApiProvider;
  }

  @Override
  public SyncRepository get() {
    return newInstance(patrimonioDaoProvider.get(), salaDaoProvider.get(), coletaDaoProvider.get(), patrimonioApiProvider.get(), coletaApiProvider.get());
  }

  public static SyncRepository_Factory create(Provider<PatrimonioDao> patrimonioDaoProvider,
      Provider<SalaDao> salaDaoProvider, Provider<ColetaDao> coletaDaoProvider,
      Provider<PatrimonioApi> patrimonioApiProvider, Provider<ColetaApi> coletaApiProvider) {
    return new SyncRepository_Factory(patrimonioDaoProvider, salaDaoProvider, coletaDaoProvider, patrimonioApiProvider, coletaApiProvider);
  }

  public static SyncRepository newInstance(PatrimonioDao patrimonioDao, SalaDao salaDao,
      ColetaDao coletaDao, PatrimonioApi patrimonioApi, ColetaApi coletaApi) {
    return new SyncRepository(patrimonioDao, salaDao, coletaDao, patrimonioApi, coletaApi);
  }
}
