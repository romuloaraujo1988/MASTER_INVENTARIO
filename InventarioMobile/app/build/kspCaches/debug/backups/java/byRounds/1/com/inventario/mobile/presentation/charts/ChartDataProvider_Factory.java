package com.inventario.mobile.presentation.charts;

import com.inventario.mobile.data.local.dao.ColetaDao;
import com.inventario.mobile.data.local.dao.PatrimonioDao;
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
public final class ChartDataProvider_Factory implements Factory<ChartDataProvider> {
  private final Provider<PatrimonioDao> patrimonioDaoProvider;

  private final Provider<ColetaDao> coletaDaoProvider;

  public ChartDataProvider_Factory(Provider<PatrimonioDao> patrimonioDaoProvider,
      Provider<ColetaDao> coletaDaoProvider) {
    this.patrimonioDaoProvider = patrimonioDaoProvider;
    this.coletaDaoProvider = coletaDaoProvider;
  }

  @Override
  public ChartDataProvider get() {
    return newInstance(patrimonioDaoProvider.get(), coletaDaoProvider.get());
  }

  public static ChartDataProvider_Factory create(Provider<PatrimonioDao> patrimonioDaoProvider,
      Provider<ColetaDao> coletaDaoProvider) {
    return new ChartDataProvider_Factory(patrimonioDaoProvider, coletaDaoProvider);
  }

  public static ChartDataProvider newInstance(PatrimonioDao patrimonioDao, ColetaDao coletaDao) {
    return new ChartDataProvider(patrimonioDao, coletaDao);
  }
}
