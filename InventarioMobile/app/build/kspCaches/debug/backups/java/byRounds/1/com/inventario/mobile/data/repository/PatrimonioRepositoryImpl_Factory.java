package com.inventario.mobile.data.repository;

import com.inventario.mobile.data.local.dao.PatrimonioDao;
import com.inventario.mobile.data.mapper.PatrimonioMapper;
import com.inventario.mobile.data.remote.api.PatrimonioApi;
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
public final class PatrimonioRepositoryImpl_Factory implements Factory<PatrimonioRepositoryImpl> {
  private final Provider<PatrimonioDao> patrimonioDaoProvider;

  private final Provider<PatrimonioApi> patrimonioApiProvider;

  private final Provider<PatrimonioMapper> mapperProvider;

  public PatrimonioRepositoryImpl_Factory(Provider<PatrimonioDao> patrimonioDaoProvider,
      Provider<PatrimonioApi> patrimonioApiProvider, Provider<PatrimonioMapper> mapperProvider) {
    this.patrimonioDaoProvider = patrimonioDaoProvider;
    this.patrimonioApiProvider = patrimonioApiProvider;
    this.mapperProvider = mapperProvider;
  }

  @Override
  public PatrimonioRepositoryImpl get() {
    return newInstance(patrimonioDaoProvider.get(), patrimonioApiProvider.get(), mapperProvider.get());
  }

  public static PatrimonioRepositoryImpl_Factory create(
      Provider<PatrimonioDao> patrimonioDaoProvider, Provider<PatrimonioApi> patrimonioApiProvider,
      Provider<PatrimonioMapper> mapperProvider) {
    return new PatrimonioRepositoryImpl_Factory(patrimonioDaoProvider, patrimonioApiProvider, mapperProvider);
  }

  public static PatrimonioRepositoryImpl newInstance(PatrimonioDao patrimonioDao,
      PatrimonioApi patrimonioApi, PatrimonioMapper mapper) {
    return new PatrimonioRepositoryImpl(patrimonioDao, patrimonioApi, mapper);
  }
}
