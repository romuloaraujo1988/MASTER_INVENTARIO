package com.inventario.mobile.data.strategy;

import android.content.Context;
import com.inventario.mobile.api.PatrimonioApi;
import com.inventario.mobile.api.SalaApi;
import com.inventario.mobile.data.local.dao.PatrimonioDao;
import com.inventario.mobile.data.local.dao.SalaDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DataSourceStrategyFactory_Factory implements Factory<DataSourceStrategyFactory> {
  private final Provider<Context> contextProvider;

  private final Provider<PatrimonioApi> patrimonioApiProvider;

  private final Provider<SalaApi> salaApiProvider;

  private final Provider<PatrimonioDao> patrimonioDaoProvider;

  private final Provider<SalaDao> salaDaoProvider;

  public DataSourceStrategyFactory_Factory(Provider<Context> contextProvider,
      Provider<PatrimonioApi> patrimonioApiProvider, Provider<SalaApi> salaApiProvider,
      Provider<PatrimonioDao> patrimonioDaoProvider, Provider<SalaDao> salaDaoProvider) {
    this.contextProvider = contextProvider;
    this.patrimonioApiProvider = patrimonioApiProvider;
    this.salaApiProvider = salaApiProvider;
    this.patrimonioDaoProvider = patrimonioDaoProvider;
    this.salaDaoProvider = salaDaoProvider;
  }

  @Override
  public DataSourceStrategyFactory get() {
    return newInstance(contextProvider.get(), patrimonioApiProvider.get(), salaApiProvider.get(), patrimonioDaoProvider.get(), salaDaoProvider.get());
  }

  public static DataSourceStrategyFactory_Factory create(Provider<Context> contextProvider,
      Provider<PatrimonioApi> patrimonioApiProvider, Provider<SalaApi> salaApiProvider,
      Provider<PatrimonioDao> patrimonioDaoProvider, Provider<SalaDao> salaDaoProvider) {
    return new DataSourceStrategyFactory_Factory(contextProvider, patrimonioApiProvider, salaApiProvider, patrimonioDaoProvider, salaDaoProvider);
  }

  public static DataSourceStrategyFactory newInstance(Context context, PatrimonioApi patrimonioApi,
      SalaApi salaApi, PatrimonioDao patrimonioDao, SalaDao salaDao) {
    return new DataSourceStrategyFactory(context, patrimonioApi, salaApi, patrimonioDao, salaDao);
  }
}
