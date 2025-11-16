package com.inventario.mobile.data.repository;

import com.inventario.mobile.data.local.dao.ColetaDao;
import com.inventario.mobile.data.local.dao.PatrimonioDao;
import com.inventario.mobile.data.mapper.ColetaMapper;
import com.inventario.mobile.data.remote.api.ColetaApi;
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

  private final Provider<ColetaMapper> mapperProvider;

  public ColetaRepositoryImpl_Factory(Provider<ColetaDao> coletaDaoProvider,
      Provider<PatrimonioDao> patrimonioDaoProvider, Provider<ColetaApi> coletaApiProvider,
      Provider<ColetaMapper> mapperProvider) {
    this.coletaDaoProvider = coletaDaoProvider;
    this.patrimonioDaoProvider = patrimonioDaoProvider;
    this.coletaApiProvider = coletaApiProvider;
    this.mapperProvider = mapperProvider;
  }

  @Override
  public ColetaRepositoryImpl get() {
    return newInstance(coletaDaoProvider.get(), patrimonioDaoProvider.get(), coletaApiProvider.get(), mapperProvider.get());
  }

  public static ColetaRepositoryImpl_Factory create(Provider<ColetaDao> coletaDaoProvider,
      Provider<PatrimonioDao> patrimonioDaoProvider, Provider<ColetaApi> coletaApiProvider,
      Provider<ColetaMapper> mapperProvider) {
    return new ColetaRepositoryImpl_Factory(coletaDaoProvider, patrimonioDaoProvider, coletaApiProvider, mapperProvider);
  }

  public static ColetaRepositoryImpl newInstance(ColetaDao coletaDao, PatrimonioDao patrimonioDao,
      ColetaApi coletaApi, ColetaMapper mapper) {
    return new ColetaRepositoryImpl(coletaDao, patrimonioDao, coletaApi, mapper);
  }
}
