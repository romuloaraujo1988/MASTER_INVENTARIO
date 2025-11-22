package com.inventario.mobile.data.mapper;

import com.inventario.mobile.data.local.dao.PatrimonioDao;
import com.inventario.mobile.utils.PreferencesManager;
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
public final class ColetaMapper_Factory implements Factory<ColetaMapper> {
  private final Provider<PatrimonioDao> patrimonioDaoProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public ColetaMapper_Factory(Provider<PatrimonioDao> patrimonioDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.patrimonioDaoProvider = patrimonioDaoProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  @Override
  public ColetaMapper get() {
    return newInstance(patrimonioDaoProvider.get(), preferencesManagerProvider.get());
  }

  public static ColetaMapper_Factory create(Provider<PatrimonioDao> patrimonioDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new ColetaMapper_Factory(patrimonioDaoProvider, preferencesManagerProvider);
  }

  public static ColetaMapper newInstance(PatrimonioDao patrimonioDao,
      PreferencesManager preferencesManager) {
    return new ColetaMapper(patrimonioDao, preferencesManager);
  }
}
