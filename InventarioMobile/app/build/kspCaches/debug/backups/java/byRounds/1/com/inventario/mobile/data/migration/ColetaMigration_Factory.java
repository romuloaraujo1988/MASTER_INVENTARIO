package com.inventario.mobile.data.migration;

import com.inventario.mobile.data.local.dao.ColetaDao;
import com.inventario.mobile.data.local.dao.PatrimonioDao;
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
public final class ColetaMigration_Factory implements Factory<ColetaMigration> {
  private final Provider<ColetaDao> coletaDaoProvider;

  private final Provider<PatrimonioDao> patrimonioDaoProvider;

  public ColetaMigration_Factory(Provider<ColetaDao> coletaDaoProvider,
      Provider<PatrimonioDao> patrimonioDaoProvider) {
    this.coletaDaoProvider = coletaDaoProvider;
    this.patrimonioDaoProvider = patrimonioDaoProvider;
  }

  @Override
  public ColetaMigration get() {
    return newInstance(coletaDaoProvider.get(), patrimonioDaoProvider.get());
  }

  public static ColetaMigration_Factory create(Provider<ColetaDao> coletaDaoProvider,
      Provider<PatrimonioDao> patrimonioDaoProvider) {
    return new ColetaMigration_Factory(coletaDaoProvider, patrimonioDaoProvider);
  }

  public static ColetaMigration newInstance(ColetaDao coletaDao, PatrimonioDao patrimonioDao) {
    return new ColetaMigration(coletaDao, patrimonioDao);
  }
}
