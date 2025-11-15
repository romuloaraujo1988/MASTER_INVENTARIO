package com.inventario.mobile.data.repository;

import com.inventario.mobile.api.SalaApi;
import com.inventario.mobile.data.local.dao.SalaDao;
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
public final class SalaRepositoryImpl_Factory implements Factory<SalaRepositoryImpl> {
  private final Provider<SalaApi> salaApiProvider;

  private final Provider<SalaDao> salaDaoProvider;

  public SalaRepositoryImpl_Factory(Provider<SalaApi> salaApiProvider,
      Provider<SalaDao> salaDaoProvider) {
    this.salaApiProvider = salaApiProvider;
    this.salaDaoProvider = salaDaoProvider;
  }

  @Override
  public SalaRepositoryImpl get() {
    return newInstance(salaApiProvider.get(), salaDaoProvider.get());
  }

  public static SalaRepositoryImpl_Factory create(Provider<SalaApi> salaApiProvider,
      Provider<SalaDao> salaDaoProvider) {
    return new SalaRepositoryImpl_Factory(salaApiProvider, salaDaoProvider);
  }

  public static SalaRepositoryImpl newInstance(SalaApi salaApi, SalaDao salaDao) {
    return new SalaRepositoryImpl(salaApi, salaDao);
  }
}
