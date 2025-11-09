package com.inventario.mobile.data.repository;

import com.inventario.mobile.data.local.dao.SincronizacaoDao;
import com.inventario.mobile.data.mapper.SincronizacaoMapper;
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
public final class SincronizacaoRepositoryImpl_Factory implements Factory<SincronizacaoRepositoryImpl> {
  private final Provider<SincronizacaoDao> sincronizacaoDaoProvider;

  private final Provider<SincronizacaoMapper> mapperProvider;

  public SincronizacaoRepositoryImpl_Factory(Provider<SincronizacaoDao> sincronizacaoDaoProvider,
      Provider<SincronizacaoMapper> mapperProvider) {
    this.sincronizacaoDaoProvider = sincronizacaoDaoProvider;
    this.mapperProvider = mapperProvider;
  }

  @Override
  public SincronizacaoRepositoryImpl get() {
    return newInstance(sincronizacaoDaoProvider.get(), mapperProvider.get());
  }

  public static SincronizacaoRepositoryImpl_Factory create(
      Provider<SincronizacaoDao> sincronizacaoDaoProvider,
      Provider<SincronizacaoMapper> mapperProvider) {
    return new SincronizacaoRepositoryImpl_Factory(sincronizacaoDaoProvider, mapperProvider);
  }

  public static SincronizacaoRepositoryImpl newInstance(SincronizacaoDao sincronizacaoDao,
      SincronizacaoMapper mapper) {
    return new SincronizacaoRepositoryImpl(sincronizacaoDao, mapper);
  }
}
